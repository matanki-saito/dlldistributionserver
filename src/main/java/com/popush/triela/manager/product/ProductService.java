package com.popush.triela.manager.product;

import com.popush.triela.common.exception.OtherSystemException;
import com.popush.triela.common.github.GitHubApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final GitHubApiService gitHubApiService;

    public List<ProductForm> list() throws OtherSystemException {
        return gitHubApiService.getMyAdminRepos().stream().map(
                elem -> ProductForm
                        .builder()
                        .gitHubRepositoryId(elem.getId())
                        .gitHubRepositoryName(elem.getFullName())
                        .build()
        ).collect(Collectors.toList());
    }
}
