package com.popush.triela.manager.product;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.popush.triela.common.github.GitHubReposResponse;

@Component
public class ProductionControllerSupport {
    public ProductView makeProductView(List<GitHubReposResponse> repos,
                                       Pageable pageable,
                                       ProductSearchConditionForm conditionForm) {

        // listをフィルタ。
        List<ProductView.Element> convertedRepos = repos
                .stream()
                .filter(item -> {
                    // push権限でフィルタ。これは絶対条件
                    if (!item.getPermissions().containsKey("push")
                        || !item.getPermissions().get("push")) {
                        return false;
                    }

                    // 名前でフィルタ
                    if (Strings.isBlank(conditionForm.getName())) {
                        return true;
                    } else {
                        return item.getFullName().toLowerCase().contains(conditionForm.getName().toLowerCase());
                    }
                })
                .map(x -> ProductView.Element
                        .builder()
                        .gitHubRepositoryId(x.getId())
                        .gitHubRepositoryName(x.getFullName())
                        .build())
                .collect(Collectors.toList());

        // pageableに合わせてsliceする
        int beginIndex = (int) pageable.getOffset();
        int endIndex = Math.min(beginIndex + pageable.getPageSize(), convertedRepos.size());

        return ProductView.builder()
                          .conditionForm(conditionForm)
                          .pageData(new PageImpl<>(
                                  convertedRepos.subList(beginIndex, endIndex),
                                  pageable,
                                  convertedRepos.size()))
                          .build();
    }
}
