package com.popush.triela.common.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@Data
class GitHubAssetResponse {
    private String url;

    @JsonProperty("browser_download_url")
    private String browserDownloadUrl;

    @JsonProperty("size")
    private int fileSize;
}
