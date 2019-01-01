package com.popush.triela.common.github;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
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
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(GitHubReposResponse.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) throws Exception {

        HttpServletRequest httpServletRequest = webRequest.getNativeRequest(HttpServletRequest.class);

        // nullチェック
        if (httpServletRequest == null) {
            return new IllegalArgumentException();
        }

        Map pathVariables = (Map) httpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        // 何もしない
        if (pathVariables.get("gitHubMyRepoId") == null) {
            log.debug("パラメタなし");
            return new IllegalArgumentException();
        }

        final int gitHubRepoId = Integer.parseInt(pathVariables.get("gitHubMyRepoId").toString());

        Optional<GitHubReposResponse> repo = gitHubApiService.getMyAdminRepos()
                .stream()
                .filter(elem -> elem.getId() == gitHubRepoId)
                .findFirst();

        // なし
        if (repo.isEmpty()) {
            return new IllegalArgumentException();
        }

        return repo.get();
    }
}
