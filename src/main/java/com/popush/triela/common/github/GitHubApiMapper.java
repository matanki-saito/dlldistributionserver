package com.popush.triela.common.github;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface GitHubApiMapper {
    @GET("repos/{owner}/{repo_name}/releases")
    Call<List<GitHubReleaseResponse>> releases(@Header("Authorization") String token,
                                               @Path("owner") String owner,
                                               @Path("repo_name") String repoName);

    @GET("repos/{owner}/{repo_name}/releases/assets/{asset_id}")
    Call<GitHubAssetResponse> asset(@Header("Authorization") String token,
                                    @Path("owner") String owner,
                                    @Path("repo_name") String repoName,
                                    @Path("asset_id") int asset_id);

    @GET("user/repos")
    Call<List<GitHubReposResponse>> repos(@Header("Authorization") String token);

    @GET
    @Streaming
    Call<ResponseBody> downloadFileWithDynamicUrlSync(@Url String fileUrl);
}
