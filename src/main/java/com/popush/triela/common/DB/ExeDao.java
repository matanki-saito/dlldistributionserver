package com.popush.triela.common.DB;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExeDao {
    @NonNull
    private String product;
    @NonNull
    private String md5;
    @NonNull
    private String version;
    private String description;
    private Integer distributionAssetId;
}