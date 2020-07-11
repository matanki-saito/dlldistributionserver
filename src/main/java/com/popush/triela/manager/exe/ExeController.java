package com.popush.triela.manager.exe;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.popush.triela.common.db.ExeEntity;
import com.popush.triela.common.db.ExeSelectCondition;
import com.popush.triela.common.exception.ArgumentException;
import com.popush.triela.common.exception.OtherSystemException;
import com.popush.triela.common.github.GitHubReposResponse;
import com.popush.triela.db.ExeMapper;
import com.popush.triela.manager.TrielaManagerV1Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ExeController extends TrielaManagerV1Controller {
    private final ExeControllerSupport exeControllerSupport;
    private final ExeMapper exeMapper;

    @Transactional(readOnly = true)
    @GetMapping("product/{gitHubMyRepoId}/exe")
    public String getProduct(
            @PathVariable("gitHubMyRepoId") int gitHubRepoId,
            Model model,
            Pageable pageable,
            @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient
    ) throws OtherSystemException, ArgumentException {
        var reposResponse = getHavingPushAuthorityRepo(gitHubRepoId, authorizedClient);

        var condition = ExeSelectCondition.builder()
                                          .gitHubRepoId(gitHubRepoId)
                                          .build();
        var exeEntities = exeMapper.selectByCondition(condition,
                                                      pageable.getOffset(),
                                                      pageable.getPageSize());

        var limitedAllCount = exeMapper.countByConditionWithLimit(condition,
                                                                  1000L);

        var exeView = exeControllerSupport.makeExeView(exeEntities,
                                                       pageable,
                                                       reposResponse,
                                                       limitedAllCount);

        // thymleafで表示
        model.addAttribute("view", exeView);
        model.addAttribute("form", new ExeForm());
        return "exe";
    }

    @Transactional
    @PostMapping("product/{gitHubRepoId}/exe")
    public String postProduct(
            @PathVariable("gitHubRepoId") int gitHubRepoId,
            @Validated ExeForm exeForm,
            @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient
    ) throws OtherSystemException, ArgumentException {
        getHavingPushAuthorityRepo(gitHubRepoId, authorizedClient);

        exeMapper.insert(ExeEntity.builder()
                                  .gitHubRepoId(gitHubRepoId)
                                  .md5(exeForm.getMd5())
                                  .autoUpdate(exeForm.isAutoUpdate())
                                  .version(exeForm.getVersion())
                                  .description(exeForm.getDescription())
                                  .phase(exeForm.getPhase().isBlank() ? "prod" : exeForm.getPhase())
                                  .build()
        );

        return "redirect:exe";
    }

    @Transactional
    @PostMapping("product/{gitHubRepoId}/delete/{id}")
    public String postProductDelete(
            @PathVariable("gitHubRepoId") int gitHubRepoId,
            @PathVariable("id") int id,
            @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient
    ) throws OtherSystemException, ArgumentException {
        getHavingPushAuthorityRepo(gitHubRepoId, authorizedClient);
        hasOwnId(id, gitHubRepoId);

        exeMapper.delete(id);

        return "redirect:../exe";
    }

    @Transactional
    @PostMapping("product/{gitHubRepoId}/autoUpdate/{id}")
    public String postProductAutoUpdate(
            @PathVariable("gitHubRepoId") int gitHubRepoId,
            @PathVariable("id") int id,
            @RequestParam("autoUpdate") boolean autoUpdate,
            @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient
    ) throws OtherSystemException, ArgumentException {
        getHavingPushAuthorityRepo(gitHubRepoId, authorizedClient);
        hasOwnId(id, gitHubRepoId);

        exeMapper.update(
                ExeSelectCondition.builder()
                                  .id(id)
                                  .build(),
                ExeEntity.builder()
                         .autoUpdate(autoUpdate)
                         .build()
        );

        return "redirect:../exe";
    }

    private GitHubReposResponse getHavingPushAuthorityRepo(int gitHubRepoId, OAuth2AuthorizedClient authorizedClient)
            throws OtherSystemException, ArgumentException {
        // 権限チェック
        var repoInfo = exeControllerSupport.hasPushAuthorityRepoInfo(gitHubRepoId, authorizedClient);
        if (repoInfo.isEmpty()) {
            throw new ArgumentException("You don't have `push` authority");
        }
        return repoInfo.get();
    }

    private void hasOwnId(int id, int gitHubRepoId) throws ArgumentException {
        // ID所有者チェック
        var exeEntity = exeMapper.selectById(id);
        if (exeEntity.isEmpty()) {
            throw new ArgumentException("Id is not found");
        }
        if (exeEntity.get().getGitHubRepoId() != gitHubRepoId) {
            throw new ArgumentException("authenticate error");
        }
    }
}
