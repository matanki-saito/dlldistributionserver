package com.popush.triela.manager.distribution;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class AssetForm {
    @NotNull
    private String name;

    @NotNull
    private String url;

    @NotNull
    private String assetId;

    @NotNull
    private Boolean draft;

    @NotNull
    private Boolean preRelease;

    @NotNull
    private String distributionAssetId;
}
