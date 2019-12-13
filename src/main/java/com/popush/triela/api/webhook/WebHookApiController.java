package com.popush.triela.api.webhook;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.popush.triela.api.TrielaApiV1Controller;
import com.popush.triela.common.exception.ArgumentException;
import com.popush.triela.common.exception.GitHubResourceException;
import com.popush.triela.common.exception.MachineException;
import com.popush.triela.common.exception.OtherSystemException;
import com.popush.triela.common.github.GitHubReleaseWebhookResponse;
import java.io.IOException;
import java.util.Optional;
import javax.annotation.PostConstruct;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class WebHookApiController extends TrielaApiV1Controller {
  private final WebHookAsyncService webHookAsyncService;

  @Value("${webhook.secret}")
  private String secret;

  private HmacUtils mac;

  @PostConstruct
  void setup() {
    mac = new HmacUtils(HmacAlgorithms.HMAC_SHA_1, secret);
  }

  @ResponseBody
  @PostMapping("/webhook/release/{token}")
  public String release(@RequestHeader(value = "X-GitHub-Event") String xGitHubEvent,
                        @RequestHeader(value = "X-GitHub-Delivery") String xGitHubDelivery,
                        @RequestHeader(value = "X-Hub-Signature") String xHubSignature,
                        @PathVariable("token") String personalAccessToken,
                        @RequestBody String payload
  ) throws OtherSystemException, InterruptedException, ArgumentException {
    if (!checkHmac(xHubSignature, payload)) {
      log.info("invalid");
      return "hmac is invalid";
    }

    final var token = String.format("token %s", personalAccessToken);

    var webhookResponse = findWebhookResponse(xGitHubEvent, payload);
    if (webhookResponse.isEmpty()) {
      log.info("not released notice");
      return "not released";
    }

    var process = webHookAsyncService.release(token, webhookResponse.get());

    process
        .thenAcceptAsync(heavyProcessResult -> log.warn("finished"))
        .exceptionally(e -> {
          log.warn(e.getMessage());
          return null;
        });

    return "do release";
  }

  private boolean checkHmac(@NonNull String xHubSignature, @NonNull String payload) {
    var keyValue = xHubSignature.split("=");

    return keyValue.length == 2
        && keyValue[0].equalsIgnoreCase("sha1")
        && keyValue[1].length() == 40
        && mac.hmacHex(payload).equalsIgnoreCase(keyValue[1]);
  }

  private Optional<GitHubReleaseWebhookResponse> findWebhookResponse(
      String xGitHubEvent,
      String payload
  ) throws OtherSystemException {
    if (!xGitHubEvent.equalsIgnoreCase("release")) {
      return Optional.empty();
    }

    GitHubReleaseWebhookResponse result;
    try {
      final ObjectMapper mapper = new ObjectMapper();
      result = mapper.readValue(payload, GitHubReleaseWebhookResponse.class);
    } catch (JsonParseException | JsonMappingException e) {
      throw new GitHubResourceException("Unmatched format. Check github API release note.", e);
    } catch (IOException e) {
      throw new MachineException("Object mapper IO Exception", e);
    }

    if (!result.getAction().equalsIgnoreCase("published")) {
      return Optional.empty();
    }

    return Optional.of(result);
  }
}
