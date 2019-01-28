package com.popush.triela.common.github;

import com.google.common.annotations.VisibleForTesting;
import com.popush.triela.common.Exception.GitHubException;
import com.popush.triela.common.Exception.MachineException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubApiService {
    final GitHubApiMapper gitHubApiMapper;
    private final OAuth2RestTemplate auth2RestTemplate;

    /**
     * レスポンスチェック
     *
     * @param response レスポンス
     * @param <T>      レスポンスボディ型
     * @return レスポンスボディ
     * @throws GitHubException exp
     */
    private <T> T responseCheck(Response<T> response) throws GitHubException {
        if (!response.isSuccessful()) {
            throw new IllegalArgumentException("Response is not 2xx,3xx.");
        }

        if (response.code() >= 500 && response.code() < 600) {
            throw new GitHubException("Server error ?" + response.errorBody());
        }

        if (response.body() == null) {
            throw new IllegalArgumentException("Is Data empty ?");
        }

        return response.body();
    }

    /**
     * レポジトリ情報の一覧を取得
     *
     * @return レポジトリ情報の一覧
     * @throws GitHubException exp
     */
    public List<GitHubReposResponse> getMyAdminRepos() throws GitHubException {
        final Call<List<GitHubReposResponse>> request = gitHubApiMapper.repos(
                "token " + auth2RestTemplate.getAccessToken().getValue()
        );

        final List<GitHubReposResponse> result;

        final Response<List<GitHubReposResponse>> response;
        try {
            response = request.execute();
        } catch (IOException e) {
            throw new GitHubException("Cannot get repository info.", e);
        }

        final List<GitHubReposResponse> responseBody = responseCheck(response);

        // push権限を持つ
        result = responseBody
                .stream()
                .filter(elem -> elem.getPermissions().containsKey("push") && elem.getPermissions()
                        .get("push"))
                .collect(Collectors.toList());

        return result;
    }

    /**
     * リリース一覧を取得する
     *
     * @param owner    レポジトリのオーナー
     * @param repoName レポジトリ名
     * @return リリース一覧
     */
    public List<GitHubReleaseResponse> getReleasesSync(@NonNull String owner,
                                                       @NonNull String repoName) throws GitHubException {
        final Call<List<GitHubReleaseResponse>> request = gitHubApiMapper.releases(
                "token " + auth2RestTemplate.getAccessToken().getValue(),
                owner,
                repoName
        );

        final Response<List<GitHubReleaseResponse>> response;
        try {
            response = request.execute();
        } catch (IOException e) {
            throw new GitHubException("Connection error.", e);
        }

        return responseCheck(response);
    }

    /**
     * アセットのURIを取得する
     *
     * @param owner    レポジトリのオーナー
     * @param repoName レポジトリ名
     * @param assetId  アセットID
     * @return アセットのURI
     * @throws GitHubException exp
     */
    private URI getAssetDownloadUrl(@NonNull String owner,
                                    @NonNull String repoName,
                                    int assetId) throws GitHubException {
        final String result;

        final Call<GitHubAssetResponse> request = gitHubApiMapper.asset(
                "token " + auth2RestTemplate.getAccessToken().getValue(),
                owner,
                repoName,
                assetId
        );

        // 取得！
        Response<GitHubAssetResponse> response;
        try {
            response = request.execute();
        } catch (IOException e) {
            throw new GitHubException("Connection error.", e);
        }

        // チェック
        final GitHubAssetResponse gitHubAssetResponse = responseCheck(response);

        // 100MB超えてたら無理とする
        if (gitHubAssetResponse.getFileSize() > 100_000_000) {
            throw new IllegalArgumentException("File size over.");
        }

        result = gitHubAssetResponse.getBrowserDownloadUrl();

        return URI.create(result);
    }

    /**
     * URLからアセット（zipファイル）を取ってくる
     *
     * @param downloadUrl アセットのURL
     * @return 一時ファイルになったzipファイルのパス
     * @throws GitHubException  exp
     * @throws MachineException exp
     */
    @VisibleForTesting
    Path getAssetFile(@NonNull URI downloadUrl) throws GitHubException, MachineException {

        final Response<ResponseBody> response;
        try {
            response = gitHubApiMapper.downloadFileWithDynamicUrlSync(downloadUrl).execute();
        } catch (IOException e) {
            throw new GitHubException("Connection failed.", e);
        }

        final ResponseBody responseBody = responseCheck(response);

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

        return tmpFile;
    }


    /**
     * アセットを取得
     *
     * @param owner    アセットのオーナー
     * @param repoName レポジトリ名
     * @param assetId  アセットID
     * @return アセットファイル
     */
    public Path getDllFromAsset(@NonNull String owner,
                                @NonNull String repoName,
                                int assetId) {

        // アセットのURLを取得
        final URI assetUri;
        try {
            assetUri = getAssetDownloadUrl(owner, repoName, assetId);
        } catch (GitHubException e) {
            throw new IllegalStateException("Cannot asset download URI.", e);
        }

        // アセットを取得
        final Path assetFile;
        try {
            assetFile = getAssetFile(assetUri);
        } catch (GitHubException | MachineException e) {
            throw new IllegalStateException("Get asset file exception.", e);
        }

        return assetFile;
    }
}
