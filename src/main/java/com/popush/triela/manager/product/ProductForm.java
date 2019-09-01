package com.popush.triela.manager.product;

import lombok.*;
import org.springframework.data.domain.Page;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ProductForm {
    private Page<ProductElement> data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class ProductElement {
        @NotNull
        private int gitHubRepositoryId;

        @NonNull
        private String gitHubRepositoryName;
    }
}
