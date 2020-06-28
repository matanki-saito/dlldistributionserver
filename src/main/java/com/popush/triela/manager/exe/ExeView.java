package com.popush.triela.manager.exe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExeView {
    private long gitHubRepositoryId;
    private String gitHubRepositoryName;
    private Page<Element> pageData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Element {
        private int id;
        private String md5;
        private String version;
        private String description;
        private Integer distributionAssetId;
        private String phase;
        private boolean autoUpdate;
    }
}
