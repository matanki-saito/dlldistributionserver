package com.popush.triela.manager.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.popush.triela.common.github.GitHubApiService;
import com.popush.triela.common.github.GitHubReposResponse;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final GitHubApiService gitHubApiService;


}
