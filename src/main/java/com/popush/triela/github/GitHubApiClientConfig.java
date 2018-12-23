package com.popush.triela.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Configuration
public class GitHubApiClientConfig {
    @Bean
    public GitHubApiMapper gitHubApiClient(){
        final OkHttpClient client = new OkHttpClient();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        final var retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com")
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .client(client)
                .build();

        return retrofit.create(GitHubApiMapper.class);
    }
}
