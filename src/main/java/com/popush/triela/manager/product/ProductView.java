package com.popush.triela.manager.product;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.domain.Page;

@Data
@Builder
public class ProductView {
    private Page<ProductElement> pageData;

    private ProductSearchConditionForm conditionForm;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductElement {
        @NotNull
        private int gitHubRepositoryId;

        @NonNull
        private String gitHubRepositoryName;
    }
}
