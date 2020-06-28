package com.popush.triela.manager.exe;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Component;

import com.popush.triela.common.db.ExeEntity;
import com.popush.triela.common.exception.OtherSystemException;
import com.popush.triela.common.github.GitHubApiService;
import com.popush.triela.common.github.GitHubReposResponse;

@Component
@RequiredArgsConstructor
public class ExeControllerSupport {
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

    public ExeView makeExeView(List<ExeEntity> exeEntities,
                               GitHubReposResponse gitHubReposResponse) {

        var elements = exeEntities.stream()
                                  .map(src -> {
                                      var dest = new ExeView.Element();
                                      BeanUtils.copyProperties(src, dest);
                                      return dest;
                                  })
                                  .collect(Collectors.toList());

        return ExeView.builder()
                      .gitHubRepositoryId(gitHubReposResponse.getId())
                      .gitHubRepositoryName(gitHubReposResponse.getFullName())
                      .pageData(new PageImpl<>(
                              elements,
                              Pageable.unpaged(),
                              elements.size()
                      ))
                      .build();
    }
}
