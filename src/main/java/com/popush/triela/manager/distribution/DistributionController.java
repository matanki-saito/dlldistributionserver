package com.popush.triela.manager.distribution;

import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.popush.triela.common.db.ExeSelectCondition;
import com.popush.triela.common.exception.OtherSystemException;
import com.popush.triela.common.github.GitHubReposResponse;
import com.popush.triela.db.ExeMapper;
import com.popush.triela.manager.TrielaManagerV1Controller;

@Controller
@RequiredArgsConstructor
public class DistributionController extends TrielaManagerV1Controller {

    private final DistributionService distributionMgrService;
    private final ExeMapper exeMapper;


    @GetMapping("product/{gitHubMyRepoId}/distribution")
    public String distributionGet(
            Model model,
            @PathVariable("gitHubMyRepoId") int gitHubMyRepoId,
            GitHubReposResponse gitHubReposResponse
    ) throws OtherSystemException {
        final var token = String.format("token %s", "");
        final var condition = ExeSelectCondition
                .builder()
                .gitHubRepoId(gitHubReposResponse.getId())
                .build();

        model.addAttribute("gitHubRepositoryName", gitHubReposResponse.getFullName());
        model.addAttribute("gitHubRepositoryId", gitHubReposResponse.getId());
        model.addAttribute("assetList", distributionMgrService.list(gitHubReposResponse, token));
        model.addAttribute("exeRegisterList", exeMapper.list(condition));

        return "distribution";
    }


    @PostMapping("product/{gitHubMyRepoId}/distribution")
    public String distributionPost(
            @RequestParam MultiValueMap<String, String> params, Model model,
            @PathVariable("gitHubMyRepoId") int gitHubMyRepoId,
            GitHubReposResponse gitHubReposResponse
    ) throws OtherSystemException {

        /* exe-id:asset-id */
        final Map<Integer, Integer> exeId2assetIdMap = params.entrySet().stream()
                                                             .filter(e -> !e.getKey().equals("_csrf"))
                                                             .collect(Collectors.toMap(
                                                                     e -> Integer.parseInt(e.getKey()),
                                                                     e -> Integer.parseInt(e.getValue().get(0))
                                                             ));

        final var token = String.format("token %s", "");

        distributionMgrService.update(
                exeId2assetIdMap,
                gitHubReposResponse.getOwner().getLogin(),
                gitHubReposResponse.getName(),
                gitHubReposResponse.getId(),
                token
        );

        return "redirect:distribution";
    }
}
