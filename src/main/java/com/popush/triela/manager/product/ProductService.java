package com.popush.triela.manager.product;

import com.popush.triela.common.exception.OtherSystemException;
import com.popush.triela.common.github.GitHubApiService;
import com.popush.triela.common.github.GitHubReposResponse;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final GitHubApiService gitHubApiService;

    public ProductForm page(ProductSearchConditionForm condition, Pageable pageable) throws OtherSystemException {
        List<GitHubReposResponse> originalList = gitHubApiService.getMyAdminRepos();

        var convertList = originalList
                .stream()
                .filter(item -> {
                    if (Strings.isNotBlank(condition.getName())) {
                        return item.getFullName().contains(condition.getName());
                    }
                    return true;
                })
                .map(ProductService::convertToProductElement)
                .collect(Collectors.toList());

        int beginIndex = (int) pageable.getOffset();
        int endIndex = Math.min(beginIndex + pageable.getPageSize(), convertList.size());

        return ProductForm.builder().data(new PageImpl<>(
                convertList.subList(beginIndex, endIndex),
                pageable,
                convertList.size()
        )).build();
    }

    private static ProductForm.ProductElement convertToProductElement(GitHubReposResponse entity) {
        return ProductForm.ProductElement
                .builder()
                .gitHubRepositoryId(entity.getId())
                .gitHubRepositoryName(entity.getFullName())
                .build();
    }

}
