package com.popush.triela.common.github;

import com.popush.triela.common.exception.ArgumentException;
import com.popush.triela.common.exception.OtherSystemException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubReposResolver implements HandlerMethodArgumentResolver {
    private final GitHubApiService gitHubApiService;

    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        return parameter.getParameterType().equals(GitHubReposResponse.class);
    }

    @Override
    public Object resolveArgument(
            @NonNull MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            @NonNull NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) throws OtherSystemException {

        HttpServletRequest httpServletRequest = webRequest.getNativeRequest(HttpServletRequest.class);

        // nullチェック
        if (httpServletRequest == null) {
            return new ArgumentException("request httpservlet is null.", httpServletRequest);
        }

        Map pathVariables = (Map) httpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        // 何もしない
        if (pathVariables.get("gitHubMyRepoId") == null) {
            return new ArgumentException("pathVariables do not include gitHubMyRepoId", pathVariables);
        }

        final int gitHubRepoId = Integer.parseInt(pathVariables.get("gitHubMyRepoId").toString());

        Optional<GitHubReposResponse> repo = gitHubApiService.getMyAdminRepos()
                .stream()
                .filter(elem -> elem.getId() == gitHubRepoId)
                .findFirst();

        // なし
        if (repo.isEmpty()) {
            return new ArgumentException("repo is empty", httpServletRequest);
        }

        return repo.get();
    }
}
