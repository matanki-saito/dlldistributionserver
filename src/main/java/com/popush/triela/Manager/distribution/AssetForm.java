package com.popush.triela.Manager.distribution;

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
}
