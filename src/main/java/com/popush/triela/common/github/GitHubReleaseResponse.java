package com.popush.triela.common.github;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@Data
public class GitHubReleaseResponse implements Serializable {
    private int id;
    @JsonProperty("html_url")
    private String htmlUrl;
    private String name;

    private Boolean draft;

    @JsonProperty("prerelease")
    private Boolean preRelease;

    @JsonProperty("created_at")
    private ZonedDateTime createdAt;

    @JsonProperty("published_at")
    private ZonedDateTime publishedAt;

    private List<Asset> assets;
    private String body;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    @Data
    public static class Asset implements Serializable {
        private int id;
        private String name;
        private int size;
        @JsonProperty("content_type")
        private String contentType;
        @JsonProperty("browser_download_url")
        private String browserDownloadUrl;
    }
}
