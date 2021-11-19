package com.popush.triela.manager.distribution;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.popush.triela.common.db.ExeSelectCondition;
import com.popush.triela.common.exception.ArgumentException;
import com.popush.triela.common.exception.OtherSystemException;
import com.popush.triela.common.github.GitHubApiService;
import com.popush.triela.common.github.GitHubReleaseResponse;
import com.popush.triela.common.github.GitHubReposResponse;
import com.popush.triela.db.ExeMapper;
import com.popush.triela.manager.TrielaManagerV1Controller;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DistributionController extends TrielaManagerV1Controller {
    private final DistributionService distributionMgrService;
    private final ExeMapper exeMapper;
    private final DistributionControllerSupport controllerSupport;
    private final GitHubApiService gitHubApiService;

    @GetMapping("product/{gitHubRepoId}/distribution")
    public String distributionGet(
            @PathVariable("gitHubRepoId") int gitHubRepoId,
            Model model,
            Pageable pageable,
            @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient
    ) throws OtherSystemException, ArgumentException {
        var authorityRepo = getHavingPushAuthorityRepo(gitHubRepoId, authorizedClient);

        // 横軸
        var exeEntities = exeMapper.selectByCondition(ExeSelectCondition.builder()
                                                                        .gitHubRepoId(gitHubRepoId)
                                                                        .build(),
                                                      0L, 10);

        // 縦軸
        final List<GitHubReleaseResponse> allReleases = gitHubApiService.getReleasesSync(
                authorityRepo.getOwner().getLogin(),
                authorityRepo.getName(),
                authorizedClient.getAccessToken().getTokenValue()
        );

        var view = controllerSupport.makeDistributionView(authorityRepo,
                                                          pageable,
                                                          Pageable.unpaged(),
                                                          exeEntities,
                                                          allReleases);

        model.addAttribute("view", view);
        return "distribution";
    }

    @PostMapping("product/{gitHubRepoId}/distribution")
    public String distributionPost(
            @PathVariable("gitHubRepoId") int gitHubRepoId,
            @RequestParam MultiValueMap<String, String> params,
            @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient
    ) throws OtherSystemException, ArgumentException {
        var authorityRepo = getHavingPushAuthorityRepo(gitHubRepoId, authorizedClient);

        /* exe-id:asset-id */
        final Map<Integer, Integer> exeId2assetIdMap = params.entrySet().stream()
                                                             .filter(e -> !e.getKey().equals("_csrf"))
                                                             .collect(Collectors.toMap(
                                                                     e -> Integer.parseInt(e.getKey()),
                                                                     e -> Integer.parseInt(e.getValue().get(0))
                                                             ));

        validOwnExeIds(exeId2assetIdMap.keySet(), gitHubRepoId);

        distributionMgrService.update(
                exeId2assetIdMap,
                authorityRepo.getOwner().getLogin(),
                authorityRepo.getName(),
                authorityRepo.getId(),
                authorizedClient.getAccessToken().getTokenValue()
        );

        return "redirect:distribution";
    }

    private GitHubReposResponse getHavingPushAuthorityRepo(int gitHubRepoId,
                                                           OAuth2AuthorizedClient authorizedClient)
            throws OtherSystemException, ArgumentException {
        // 権限チェック
        var repoInfo = controllerSupport.hasPushAuthorityRepoInfo(gitHubRepoId, authorizedClient);
        if (repoInfo.isEmpty()) {
            throw new ArgumentException("You don't have `push` authority");
        }
        return repoInfo.get();
    }

    private void validOwnExeIds(Set<Integer> ids, int gitHubRepoId) throws ArgumentException {
        // ID所有者チェック
        var gitHubIdSetByIds = exeMapper.findGitHubIdSetByIds(ids);
        if (gitHubIdSetByIds.isEmpty()) {
            throw new ArgumentException("Id is not found");
        }

        if (gitHubIdSetByIds.size() > 1) {
            throw new ArgumentException("No your id is included in ids");
        }

        if (!gitHubIdSetByIds.contains(gitHubRepoId)) {
            throw new ArgumentException("Invalid id");
        }
    }
}
