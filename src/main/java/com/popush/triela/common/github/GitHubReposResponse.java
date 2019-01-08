package com.popush.triela.common.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@Data
public class GitHubReposResponse {
    private int id;
    private String name;
    @JsonProperty("full_name")
    private String fullName;
    private Map<String, Boolean> permissions;
    private Owner owner;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    @Data
    public static class Owner {
        private String login;
    }
}
