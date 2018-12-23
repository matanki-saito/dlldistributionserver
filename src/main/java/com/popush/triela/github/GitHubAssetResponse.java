package com.popush.triela.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown=true)
@NoArgsConstructor
@Data
public class GitHubAssetResponse {
    @JsonProperty("browser_download_url")
    private String browserDownloadUrl;
}
