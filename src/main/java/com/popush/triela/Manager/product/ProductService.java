package com.popush.triela.Manager.product;

import com.popush.triela.common.Exception.GitHubException;
import com.popush.triela.common.Exception.ServiceException;
import com.popush.triela.common.github.GitHubApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final GitHubApiService gitHubApiService;

    public List<ProductForm> list() throws ServiceException {
        try {
            return gitHubApiService.getMyAdminRepos().stream().map(
                    elem -> ProductForm
                            .builder()
                            .gitHubRepositoryId(elem.getId())
                            .gitHubRepositoryName(elem.getFullName())
                            .build()
            ).collect(Collectors.toList());
        } catch (GitHubException e) {
            throw new ServiceException("product", e);
        }
    }
}
