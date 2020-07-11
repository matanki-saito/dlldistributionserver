package com.popush.triela.common.github;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class GitHubReposResponse implements Serializable {
    private int id;
    private String name;
    @JsonProperty("full_name")
    private String fullName;
    private Map<String, Boolean> permissions;
    private Owner owner;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Owner implements Serializable {
        private String login;
    }
}
