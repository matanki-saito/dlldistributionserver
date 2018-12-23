package com.popush.triela.github;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

import java.util.List;

public interface GitHubApiMapper {
    @GET("repos/{owner}/{repo_name}/releases")
    Call<List<GitHubReleaseResponse>> releases(@Path("owner") String owner,
                                               @Path("repo_name") String repoName);

    @GET("repos/{owner}/{repo_name}/releases/assets/{asset_id}")
    Call<GitHubAssetResponse> asset(@Path("owner") String owner,
                                    @Path("repo_name") String repoName,
                                    @Path("asset_id") int asset_id);

    @GET
    @Streaming
    Call<ResponseBody> downloadFileWithDynamicUrlSync(@Url String fileUrl);
}
