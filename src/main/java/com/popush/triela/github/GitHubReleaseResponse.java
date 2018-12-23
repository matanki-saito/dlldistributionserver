package com.popush.triela.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown=true)
@NoArgsConstructor
@Data
public class GitHubReleaseResponse {
    private int id;
    @JsonProperty("html_url")
    private String htmlUrl;
    private String name;
    private boolean draft;
    private boolean preRelease;

    @JsonProperty("created_at")
    private ZonedDateTime createdAt;

    @JsonProperty("published_at")
    private ZonedDateTime publishedAt;

    private List<Asset> assets;

    @JsonIgnoreProperties(ignoreUnknown=true)
    @NoArgsConstructor
    @Data
    public static class Asset{
        private int id;
        private String name;
        private int size;
        @JsonProperty("content_type")
        private String contentType;
        @JsonProperty("browser_download_url")
        private String browserDownloadUrl;
    }

    private String body;
}
