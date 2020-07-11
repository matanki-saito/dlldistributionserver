package com.popush.triela.common.github;

import java.net.URI;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface GitHubApiMapper {
    @GET("repos/{owner}/{repo_name}/releases")
    Call<List<GitHubReleaseResponse>> releases(@Header("Authorization") String token,
                                               @Path("owner") String owner,
                                               @Path("repo_name") String repoName);

    @GET("repos/{owner}/{repo_name}/releases/{release_id}")
    Call<GitHubReleaseResponse> release(@Header("Authorization") String token,
                                        @Path("owner") String owner,
                                        @Path("repo_name") String repoName,
                                        @Path("release_id") Integer releaseId);

    @GET("repos/{owner}/{repo_name}/releases/assets/{asset_id}")
    Call<GitHubAssetResponse> asset(@Header("Authorization") String token,
                                    @Path("owner") String owner,
                                    @Path("repo_name") String repoName,
                                    @Path("asset_id") int assetId);


    @GET("user/repos")
    Call<List<GitHubReposResponse>> repos(@Header("Authorization") String token);

    @GET("user/repos")
    Call<List<GitHubReposResponse>> repos(@Header("Authorization") String token,
                                          @Query("page") int pageNumber,
                                          @Query("per_page") int countPerPage,
                                          @Query("affiliation") String affiliation
    );

    @GET("user")
    Call<GithubUserResponse> user(@Header("Authorization") String token);

    @GET
    @Headers("Accept: application/octet-stream")
    @Streaming
    Call<ResponseBody> downloadFileWithDynamicUrlSync(
            @Header("Authorization") String token,
            @Url URI fileUrl
    );
}
