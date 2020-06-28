package com.popush.triela.manager.product;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.popush.triela.common.exception.OtherSystemException;
import com.popush.triela.common.github.GitHubApiService;
import com.popush.triela.common.github.GitHubReposResponse;
import com.popush.triela.manager.TrielaManagerV1Controller;

@Controller
@RequiredArgsConstructor
public class ProductController extends TrielaManagerV1Controller {

    private final ProductionControllerSupport productionControllerSupport;
    private final GitHubApiService gitHubApiService;


    @GetMapping("/product")
    public String exeRegisterGet(
            ProductSearchConditionForm conditionForm,
            Model model,
            Pageable pageable,
            @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient,
            @AuthenticationPrincipal OAuth2User oauth2User
    ) throws OtherSystemException {

        // その人がownerもしくcollaboratorであるレポジトリをすべて取得
        // APIには名前で検索できたりする機能は用意されていないのですべて取得している
        final var token = String.format("token %s", authorizedClient.getAccessToken().getTokenValue());
        List<GitHubReposResponse> adminAllRepos = gitHubApiService.getMyAdminReposCached(token);

        // page組み立て時にフィルタする
        final ProductView view = productionControllerSupport.makeProductView(adminAllRepos, pageable, conditionForm);

        // view表示
        model.addAttribute("view", view);
        return "product";
    }
}
