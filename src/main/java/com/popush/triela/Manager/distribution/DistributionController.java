package com.popush.triela.Manager.distribution;

import com.popush.triela.Manager.TrielaManagerV1Controller;
import com.popush.triela.Manager.exe.ExeService;
import com.popush.triela.common.github.GitHubReposResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class DistributionController extends TrielaManagerV1Controller {

    private final DistributionService distributionMgrService;
    private final ExeService exeService;

    @GetMapping("product/{gitHubMyRepoId}/distribution")
    public String distributionGet(
            Model model,
            @PathVariable("gitHubMyRepoId") int gitHubMyRepoId,
            GitHubReposResponse gitHubReposResponse
    ) {
        model.addAttribute("gitHubRepositoryId", gitHubReposResponse.getId());
        model.addAttribute("assetList", distributionMgrService.list(gitHubReposResponse));
        model.addAttribute("exeRegisterList", exeService.list(gitHubReposResponse.getId()));

        return "distribution";
    }


    @PostMapping("product/{gitHubMyRepoId}/distribution")
    public String distributionPost(
            @RequestParam MultiValueMap<String, String> params, Model model,
            @PathVariable("gitHubMyRepoId") int gitHubMyRepoId,
            GitHubReposResponse gitHubReposResponse
    ) {

        /* exe-hash:asset-id */
        final Map<String, Integer> exeHash2assetIdMap = params.entrySet().stream()
                .filter(entry -> !entry.getKey().equals("_csrf"))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> Integer.parseInt(e.getValue().get(0))
                ));


        distributionMgrService.update(
                exeHash2assetIdMap,
                gitHubReposResponse
        );

        return "redirect:distribution";
    }
}
