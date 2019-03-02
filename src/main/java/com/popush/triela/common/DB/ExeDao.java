package com.popush.triela.common.DB;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExeDao {
    private int id;
    private int gitHubRepoId;
    @NonNull
    private String md5;
    @NonNull
    private String version;
    private String description;
    private Integer distributionAssetId;
    private String phase;
}
