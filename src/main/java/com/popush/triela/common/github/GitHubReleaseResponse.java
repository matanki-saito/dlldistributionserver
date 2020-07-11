package com.popush.triela.common.github;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
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

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonProperty("published_at")
    private LocalDateTime publishedAt;

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
