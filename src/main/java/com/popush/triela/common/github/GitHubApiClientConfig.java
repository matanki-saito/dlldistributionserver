package com.popush.triela.common.github;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Configuration
public class GitHubApiClientConfig {
    @Bean
    public GitHubApiMapper gitHubApiClient() {
        final OkHttpClient client = new OkHttpClient().newBuilder()
                                                      .addInterceptor(addQueryParamInterceptor())
                                                      .build();

        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        final var retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com")
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .client(client)
                .build();

        return retrofit.create(GitHubApiMapper.class);
    }

    private static Interceptor addQueryParamInterceptor() {
        return chain -> {
            final Request original = chain.request();
            final HttpUrl originalHttpUrl = original.url();

            final HttpUrl url = originalHttpUrl.newBuilder()
                                               .addQueryParameter("page", "1")
                                               .addQueryParameter("per_page", "100")
                                               .build();

            // Request customization: add request headers
            final Request.Builder requestBuilder = original.newBuilder()
                                                           .url(url);

            final Request request = requestBuilder.build();
            return chain.proceed(request);
        };
    }
}
