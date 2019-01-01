package com.popush.triela.common.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GitHubAuthoritiesExtractor implements AuthoritiesExtractor {

    private final UserService userService;

    @Override
    public List<GrantedAuthority> extractAuthorities(Map<String, Object> map) {
        //userService.registUser((Integer) map.get("id"));

        return AuthorityUtils.createAuthorityList("ROLE_USER");
    }
}

