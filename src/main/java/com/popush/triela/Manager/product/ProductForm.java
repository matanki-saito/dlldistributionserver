package com.popush.triela.Manager.product;

import lombok.*;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ProductForm {
    @NotNull
    private int gitHubRepositoryId;

    @NonNull
    private String gitHubRepositoryName;
}
