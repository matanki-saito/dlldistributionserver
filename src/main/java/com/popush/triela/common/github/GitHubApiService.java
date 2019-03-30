package com.popush.triela.common.github;

import com.google.common.annotations.VisibleForTesting;
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
     * レポジトリ情報の一覧を取得
     *
     * @return レポジトリ情報の一覧
     * @throws OtherSystemException exp
     */
    public List<GitHubReposResponse> getMyAdminRepos() throws OtherSystemException {
        final Call<List<GitHubReposResponse>> request = gitHubApiMapper.repos(
                "token " + auth2RestTemplate.getAccessToken().getValue()
        );

        final List<GitHubReposResponse> result;

        final Response<List<GitHubReposResponse>> response;
        try {
            response = request.execute();
        } catch (IOException e) {
            throw new GitHubServiceException("Cannot get repository info.", e);
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
     * @throws OtherSystemException exp
     */
    public List<GitHubReleaseResponse> getReleasesSync(@NonNull String owner,
                                                       @NonNull String repoName) throws OtherSystemException {

        String token = auth2RestTemplate.getAccessToken().getValue();

        final Call<List<GitHubReleaseResponse>> request = gitHubApiMapper.releases(
                "token " + token,
                owner,
                repoName
        );

        final Response<List<GitHubReleaseResponse>> response;
        try {
            response = request.execute();
        } catch (IOException e) {
            throw new GitHubServiceException("Connection error.", e);
        }

        return responseCheck(response);
    }

    /**
     * アセットのURIとフォーマットを取得する
     *
     * @param owner    レポジトリのオーナー
     * @param repoName レポジトリ名
     * @param assetId  アセットID
     * @return アセットのURIとMimeType
     * @throws OtherSystemException exp
     */
    private NetworkResource getAssetDownloadUrl(@NonNull String owner,
                                                @NonNull String repoName,
                                                int assetId) throws OtherSystemException {

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
            throw new GitHubServiceException("Connection error.", e);
        }

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
     * @return 一時ファイルになったzipファイルのパス
     * @throws OtherSystemException exp
     */
    @VisibleForTesting
    private NetworkResource getAssetFile(@NonNull GitHubApiService.NetworkResource networkResource) throws OtherSystemException {

        final Response<ResponseBody> response;
        try {
            response = gitHubApiMapper.downloadFileWithDynamicUrlSync(
                    "token " + auth2RestTemplate.getAccessToken().getValue(),
                    networkResource.getUrl()
            ).execute();
        } catch (IOException e) {
            throw new GitHubServiceException("Connection failed.", e);
        }

        final ResponseBody responseBody = responseCheck(response);

        final Path tmpFile;
        try {
            tmpFile = Files.createTempFile("triela_asset", ".tmp");
        } catch (IOException e) {
            throw new MachineException("Cannot create ExeDao temp file", e);
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
     * @param owner    アセットのオーナー
     * @param repoName レポジトリ名
     * @param assetId  アセットID
     * @return アセットファイル
     * @throws OtherSystemException exp
     */
    public NetworkResource getDllFromAsset(@NonNull String owner,
                                           @NonNull String repoName,
                                           int assetId) throws OtherSystemException {

        return getAssetFile(getAssetDownloadUrl(owner, repoName, assetId));
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
