package com.popush.triela.api.webhook;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.popush.triela.api.TrielaApiV1Controller;
import com.popush.triela.common.exception.ArgumentException;
import com.popush.triela.common.exception.GitHubResourceException;
import com.popush.triela.common.exception.MachineException;
import com.popush.triela.common.exception.OtherSystemException;
import com.popush.triela.common.github.GitHubApiService;
import com.popush.triela.common.github.GitHubReleaseWebhookResponse;
import com.popush.triela.manager.distribution.DistributionService;
import com.popush.triela.manager.exe.ExeForm;
import com.popush.triela.manager.exe.ExeService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class WebHookApiController extends TrielaApiV1Controller {

    private final GitHubApiService gitHubApiService;
    private final DistributionService distributionService;
    private final ExeService exeService;

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
    ) throws OtherSystemException, ArgumentException {
        if (!checkHmac(xHubSignature, payload)) {
            log.info("invalid");
            return "hmac is invalid";
        }

        final var token = String.format("token %s", personalAccessToken);

        var response = findWebhookResponse(xGitHubEvent, payload)
                .orElseThrow(() -> new ArgumentException("No published release"));

        var owner = response.getRepository().getOwner().getLogin();
        var repoName = response.getRepository().getName();
        var repoId = response.getRepository().getId();
        var releaseId = response.getRelease().getId();

        log.info("{} {} {} {}", owner, repoId, repoName, releaseId);

        var assetIds = gitHubApiService.getAssetIds(owner, repoName, releaseId, token);

        if (assetIds.isEmpty()) {
            throw new ArgumentException("Not found asset");
        }

        log.info(assetIds.toString());

        var list = exeService.list(repoId);

        log.info(list.toString());

        Map<Integer, Integer> mapping = list
                .stream()
                .collect(Collectors.toMap(
                        ExeForm::getId,
                        v -> assetIds.get(0)
                ));

        log.info(mapping.toString());

        distributionService.update(mapping, owner, repoName, repoId, token);

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
