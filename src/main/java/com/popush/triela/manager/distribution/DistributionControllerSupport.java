package com.popush.triela.manager.distribution;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Component;

import com.popush.triela.common.db.ExeEntity;
import com.popush.triela.common.exception.OtherSystemException;
import com.popush.triela.common.github.GitHubApiService;
import com.popush.triela.common.github.GitHubReleaseResponse;
import com.popush.triela.common.github.GitHubReposResponse;

@Component
@RequiredArgsConstructor
public class DistributionControllerSupport {
    private final GitHubApiService gitHubApiService;

    public Optional<GitHubReposResponse> hasPushAuthorityRepoInfo(int gitHubRepoId,
                                                                  OAuth2AuthorizedClient authorizedClient) throws OtherSystemException {

        return gitHubApiService.getMyAdminReposCached(authorizedClient.getAccessToken().getTokenValue())
                               .stream()
                               .filter(x -> x.getId() == gitHubRepoId
                                            && x.getPermissions().size() > 0
                                            && x.getPermissions().containsKey("push")
                                            && x.getPermissions().get("push"))
                               .findFirst();
    }

    public DistributionView makeDistributionView(GitHubReposResponse gitHubReposResponse,
                                                 Pageable assetPageable,
                                                 Pageable exePageable,
                                                 List<ExeEntity> exeEntities,
                                                 List<GitHubReleaseResponse> releases) {

        // 横軸
        var exeElements = exeEntities.stream()
                                     .map(src -> DistributionView.ExeElement.builder()
                                                                            .id(src.getId())
                                                                            .version(src.getVersion())
                                                                            .phase(src.getPhase())
                                                                            .md5(src.getMd5())
                                                                            .distributionAssetId(src.getDistributionAssetId())
                                                                            .autoUpdate(src.getAutoUpdate())
                                                                            .build())
                                     .collect(Collectors.toList());

        // 縦軸
        var assetElements = releases.stream()
                                    .map(release -> {
                                        Integer assetId = null;
                                        if (release.getAssets() != null && !release.getAssets().isEmpty()) {
                                            assetId = release.getAssets().get(0).getId();
                                        }
                                        return DistributionView.AssetElement.builder()
                                                                            .id(assetId)
                                                                            .draft(release.getDraft())
                                                                            .name(release.getName())
                                                                            .releaseUrl(release.getHtmlUrl())
                                                                            .preRelease(release.getPreRelease())
                                                                            .build();
                                    })
                                    .collect(Collectors.toList());
        // 縦軸はpageableに合わせてsliceする
        int beginIndex = (int) assetPageable.getOffset();
        int endIndex = Math.min(beginIndex + assetPageable.getPageSize(), assetElements.size());

        // 組みたて
        return DistributionView.builder()
                               .gitHubRepositoryId(gitHubReposResponse.getId())
                               .gitHubRepositoryName(gitHubReposResponse.getFullName())
                               .assetFormsPageData(new PageImpl<>(
                                       assetElements.subList(beginIndex, endIndex),
                                       assetPageable,
                                       assetElements.size()
                               ))
                               .exeRegistersPageData(new PageImpl<>(
                                       exeElements,
                                       exePageable,
                                       exeEntities.size()
                               ))
                               .build();
    }
}
