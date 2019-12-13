package com.popush.triela.manager.exe;

import com.popush.triela.common.github.GitHubReposResponse;
import com.popush.triela.manager.TrielaManagerV1Controller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
      GitHubReposResponse gitHubReposResponse,
      Pageable pageable
  ) {
    var condition = ExeSearchConditionForm.builder().gitHubRepoId(gitHubReposResponse.getId()).build();
    model.addAttribute("gitHubRepositoryName", gitHubReposResponse.getFullName());
    model.addAttribute("gitHubRepositoryId", gitHubReposResponse.getId());
    model.addAttribute("page", exeService.page(condition, pageable));
    model.addAttribute("form", exeForm);

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

  @PostMapping("product/{gitHubMyRepoId}/delete/{id}")
  public String postProductDelete(
      Model model,
      @PathVariable("gitHubMyRepoId") int gitHubMyRepoId,
      @PathVariable("id") int id,
      GitHubReposResponse gitHubReposResponse
  ) {
    exeService.delete(id);

    return "redirect:../exe";
  }

  @PostMapping("product/{gitHubMyRepoId}/autoUpdate/{id}")
  public String postProductAutoUpdate(
      Model model,
      @PathVariable("gitHubMyRepoId") int gitHubMyRepoId,
      @PathVariable("id") int id,
      @RequestParam("autoUpdate") boolean autoUpdate,
      GitHubReposResponse gitHubReposResponse
  ) {
    exeService.changeAutoUpdate(id, autoUpdate);
    return "redirect:../exe";
  }
}
