package com.popush.triela.common.github;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.google.common.annotations.VisibleForTesting;
import com.popush.triela.common.cache.CacheExpiring;
import com.popush.triela.common.exception.GitHubResourceException;
import com.popush.triela.common.exception.GitHubServiceException;
import com.popush.triela.common.exception.MachineException;
import com.popush.triela.common.exception.OtherSystemException;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubApiService {
    final GitHubApiMapper gitHubApiMapper;

    /**
     * レスポンスチェック
     *
     * @param response レスポンス
     * @param <T> レスポンスボディ　Type
     *
     * @return レスポンスボディ
     *
     * @throws OtherSystemException exp
     */
    private <T> T responseCheck(Response<T> response) throws OtherSystemException {
        if (!response.isSuccessful()) {
            throw new GitHubResourceException("Response is not 2xx,3xx.");
        }

        if (response.code() >= 500 && response.code() < 600) {
            throw new GitHubServiceException("Server error ?" + response.errorBody());
        }

        if (response.body() == null) {
            throw new MachineException("Is Data empty ?");
        }

        return response.body();
    }

    /**
     * 通信を実施する
     *
     * @param caller http request caller
     * @param <R> Response type
     *
     * @return Retrofit Response
     *
     * @throws GitHubServiceException 通信異常
     */
    private <R> Response<R> executer(Call<R> caller) throws GitHubServiceException {
        try {
            return caller.execute();
        } catch (IOException e) {
            throw new GitHubServiceException("Cannot get repository", e);
        }
    }

    /**
     * レポジトリ情報の一覧を取得
     *
     * @param token アクセストークン
     *
     * @return レポジトリ情報の一覧
     *
     * @throws OtherSystemException exp
     */
    @Cacheable("getMyAdminReposCached")
    @CacheExpiring(value = 3, unit = ChronoUnit.MINUTES)
    public List<GitHubReposResponse> getMyAdminReposCached(@NonNull String token) throws OtherSystemException {

        final String tokenHeader = String.format("token %s", token);

        final Call<List<GitHubReposResponse>> reposRequest = gitHubApiMapper.repos(tokenHeader);
        final Response<List<GitHubReposResponse>> reposResponse = executer(reposRequest);
        final List<GitHubReposResponse> reposResponseBody = responseCheck(reposResponse);
        return reposResponseBody
                .stream()
                .filter(elem -> elem.getPermissions().containsKey("push")
                                && elem.getPermissions().get("push"))
                .collect(Collectors.toList());
    }

    /**
     * レポジトリ情報の一覧を取得
     *
     * @param token アクセストークン
     *
     * @return レポジトリ情報の一覧
     *
     * @throws OtherSystemException exp
     */
    @Cacheable("getMyAdminRepos")
    @CacheExpiring(value = 3, unit = ChronoUnit.MINUTES)
    public List<GitHubReposResponse> getRepos(@NonNull String token) throws OtherSystemException {
        final Call<List<GitHubReposResponse>> request = gitHubApiMapper.repos(token);

        final Response<List<GitHubReposResponse>> response = executer(request);
        return responseCheck(response);
    }

    /**
     * リリース一覧を取得する
     *
     * @param owner レポジトリのオーナー
     * @param repoName レポジトリ名
     * @param token アクセストークン
     *
     * @return リリース一覧
     *
     * @throws OtherSystemException exp
     */
    @Cacheable(value = "getReleasesSync")
    @CacheExpiring(value = 3, unit = ChronoUnit.MINUTES)
    public List<GitHubReleaseResponse> getReleasesSync(@NonNull String owner,
                                                       @NonNull String repoName,
                                                       @NonNull String token) throws OtherSystemException {

        final String tokenHeader = String.format("token %s", token);

        final Call<List<GitHubReleaseResponse>> request = gitHubApiMapper.releases(
                tokenHeader,
                owner,
                repoName
        );

        final Response<List<GitHubReleaseResponse>> response = executer(request);
        return responseCheck(response);
    }

    /**
     * アセットのURIとフォーマットを取得する
     *
     * @param owner レポジトリのオーナー
     * @param repoName レポジトリ名
     * @param assetId アセットID
     * @param token アクセストークン
     *
     * @return アセットのURIとMimeType
     *
     * @throws OtherSystemException exp
     */
    private NetworkResource getAssetDownloadUrl(@NonNull String owner,
                                                @NonNull String repoName,
                                                int assetId,
                                                @NonNull String token) throws OtherSystemException {

        final Call<GitHubAssetResponse> request = gitHubApiMapper.asset(
                token,
                owner,
                repoName,
                assetId
        );

        // 取得！
        Response<GitHubAssetResponse> response = executer(request);

        // チェック
        final GitHubAssetResponse gitHubAssetResponse = responseCheck(response);

        // 10MB超えてたら無理とする
        if (gitHubAssetResponse.getFileSize() > 10_000_000) {
            throw new GitHubResourceException("File size is 10MB over.");
        }

        return new NetworkResource(
                URI.create(gitHubAssetResponse.getUrl()),
                gitHubAssetResponse.getContentType(),
                null,
                Path.of(gitHubAssetResponse.getName())
        );
    }

    /**
     * URLからアセット（zipファイル）を取ってくる
     *
     * @param networkResource アセットのURLとタイプ
     * @param token アクセストークン
     *
     * @return 一時ファイルになったzipファイルのパス
     *
     * @throws OtherSystemException exp
     */
    @VisibleForTesting
    private NetworkResource getAssetFile(
            @NonNull GitHubApiService.NetworkResource networkResource,
            @NonNull String token
    ) throws OtherSystemException {

        final Call<ResponseBody> request = gitHubApiMapper.downloadFileWithDynamicUrlSync(
                token,
                networkResource.getUrl()
        );

        final ResponseBody responseBody = responseCheck(executer(request));

        final Path tmpFile;
        try {
            tmpFile = Files.createTempFile("triela_asset", ".tmp");
        } catch (IOException e) {
            throw new MachineException("Cannot create a temp file", e);
        }

        try (InputStream is = responseBody.byteStream()) {
            Files.copy(is, tmpFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new MachineException("Cannot write buffer to temp file.", e);
        }

        return new NetworkResource(
                networkResource.getUrl(),
                networkResource.getContentType(),
                tmpFile,
                networkResource.getOriginalFileNamePath()
        );
    }

    /**
     * アセットを取得
     *
     * @param owner アセットのオーナー
     * @param repoName レポジトリ名
     * @param assetId アセットID
     * @param token アクセストークン
     *
     * @return アセットファイル
     *
     * @throws OtherSystemException exp
     */
    public NetworkResource getDllFromAsset(@NonNull String owner,
                                           @NonNull String repoName,
                                           int assetId,
                                           @NonNull String token) throws OtherSystemException {

        return getAssetFile(getAssetDownloadUrl(owner, repoName, assetId, token), token);
    }

    /**
     * asset idを取得する
     *
     * @param owner repositoryのオーナー名
     * @param repoName レポジトリ名
     * @param releaseId release ID
     * @param token アクセストークン
     *
     * @return asset ID
     */
    public List<Integer> getAssetIds(@NonNull String owner,
                                     @NonNull String repoName,
                                     @NonNull Integer releaseId,
                                     @NonNull String token) throws OtherSystemException {

        final var tokenHeader = String.format("token %s", token);

        final Call<GitHubReleaseResponse> request = gitHubApiMapper.release(
                tokenHeader,
                owner,
                repoName,
                releaseId
        );
        final Response<GitHubReleaseResponse> response = executer(request);
        final GitHubReleaseResponse content = responseCheck(response);

        return content
                .getAssets()
                .stream()
                .map(GitHubReleaseResponse.Asset::getId)
                .collect(Collectors.toList());
    }

    @Value
    @AllArgsConstructor
    public static class NetworkResource {
        private URI url;
        private String contentType;
        private Path path;
        private Path originalFileNamePath;
    }

}
