package com.popush.triela.manager.distribution;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

@Data
@Builder
public class DistributionView {
    private String gitHubRepositoryName;
    private long gitHubRepositoryId;

    private Page<ExeElement> exeRegistersPageData;
    private Page<AssetElement> assetFormsPageData;

    @Data
    @Builder
    public static class ExeElement {
        private long id;
        private String phase;
        private String version;
        private boolean autoUpdate;
        private Integer distributionAssetId;
        private String md5;
    }

    @Data
    @Builder
    public static class AssetElement {
        private Integer id;
        private String releaseUrl;
        private String name;
        private boolean preRelease;
        private boolean draft;
    }
}
