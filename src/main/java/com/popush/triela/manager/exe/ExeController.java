package com.popush.triela.manager.exe;

import com.popush.triela.manager.TrielaManagerV1Controller;
import com.popush.triela.common.github.GitHubReposResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ExeController extends TrielaManagerV1Controller {
    private final ExeService exeService;

    @GetMapping("product/{gitHubMyRepoId}/exe")
    public String getProduct(
            ExeForm exeForm,
            Model model,
            @PathVariable("gitHubMyRepoId") int gitHubMyRepoId,
            GitHubReposResponse gitHubReposResponse
    ) {
        model.addAttribute("gitHubRepositoryId", gitHubReposResponse.getId());
        model.addAttribute("form", exeForm);
        model.addAttribute("list", exeService.list(gitHubReposResponse.getId()));

        return "exe";
    }

    @PostMapping("product/{gitHubMyRepoId}/exe")
    public String postProduct(
            @Validated ExeForm exeForm,
            Model model,
            @PathVariable("gitHubMyRepoId") int gitHubMyRepoId,
            GitHubReposResponse gitHubReposResponse
    ) {
        exeService.save(exeForm, gitHubMyRepoId);

        return "redirect:exe";
    }
}
