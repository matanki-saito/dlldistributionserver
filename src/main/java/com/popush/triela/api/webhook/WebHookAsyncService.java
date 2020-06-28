package com.popush.triela.api.webhook;

import com.popush.triela.common.db.ExeSelectCondition;
import com.popush.triela.common.exception.ArgumentException;
import com.popush.triela.common.exception.OtherSystemException;
import com.popush.triela.common.github.GitHubApiService;
import com.popush.triela.common.github.GitHubReleaseWebhookResponse;
import com.popush.triela.manager.distribution.DistributionService;
import com.popush.triela.manager.exe.ExeForm;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebHookAsyncService {
  private final GitHubApiService gitHubApiService;
  private final DistributionService distributionService;
  private final ExeService exeService;

  @Async
  public CompletableFuture<String> release(
      String token,
      GitHubReleaseWebhookResponse response
  ) throws InterruptedException, ArgumentException, OtherSystemException {
    Thread.sleep(30_000);

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

    var list = exeService.list(
        ExeSelectCondition
            .builder()
            .gitHubRepoId(repoId)
            .autoUpdate(true)
            .build()
    );

    log.info(list.toString());

    Map<Integer, Integer> mapping = list
        .stream()
        .collect(Collectors.toMap(
            ExeForm::getId,
            v -> assetIds.get(0)
        ));

    log.info(mapping.toString());

    distributionService.update(mapping, owner, repoName, repoId, token);

    return CompletableFuture.completedFuture("success");
  }
}
