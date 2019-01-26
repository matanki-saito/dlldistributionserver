package com.popush.triela.common.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.popush.triela.Manager.distribution.DistFileFormatV1;
import com.popush.triela.common.Exception.GitHubException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubApiService {
    private static S3Client s3;
    final GitHubApiMapper gitHubApiMapper;
    private final OAuth2RestTemplate auth2RestTemplate;

    public List<GitHubReposResponse> getMyAdminRepos() throws GitHubException {
        final Call<List<GitHubReposResponse>> request = gitHubApiMapper.repos(
                "token " + auth2RestTemplate.getAccessToken().getValue()
        );

        final List<GitHubReposResponse> result;
        try {
            final Response<List<GitHubReposResponse>> response = request.execute();
            if (!response.isSuccessful()) {
                throw new GitHubException("github api error:" + response.message());
            }

            if (response.body() == null) {
                throw new IllegalStateException("state error");
            }

            // push権限を持つ
            result = response.body()
                    .stream()
                    .filter(elem -> elem.getPermissions().containsKey("push") && elem.getPermissions()
                            .get("push"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException("IOError", e);
        }

        return result;
    }

    public List<GitHubReleaseResponse> getReleasesSync(@NonNull String owner,
                                                       @NonNull String repoName) {
        final Call<List<GitHubReleaseResponse>> request = gitHubApiMapper.releases(
                "token " + auth2RestTemplate.getAccessToken().getValue(),
                owner,
                repoName
        );

        final List<GitHubReleaseResponse> result;
        try {
            final Response<List<GitHubReleaseResponse>> response = request.execute();
            if (!response.isSuccessful()) {
                throw new IllegalStateException("not success");
            }
            result = response.body();

        } catch (IOException e) {
            throw new IllegalStateException("ER", e);
        }

        return result;
    }

    /**
     * @param owner
     * @param repoName
     * @param assetId
     * @return
     */
    private String getAssetDownloadUrl(@NonNull String owner,
                                       @NonNull String repoName,
                                       int assetId) {
        final String result;

        final Call<GitHubAssetResponse> request = gitHubApiMapper.asset(
                "token " + auth2RestTemplate.getAccessToken().getValue(),
                owner,
                repoName,
                assetId
        );
        try {
            Response<GitHubAssetResponse> response = request.execute();
            if (!response.isSuccessful()) {
                throw new IllegalStateException("not success");
            }
            if (response.body() == null) {
                throw new IllegalArgumentException("not success");
            }

            // 100MB超えてたら無理
            if (response.body().getFileSize() > 100_000_000) {
                throw new IllegalArgumentException("file size over.");
            }

            result = response.body().getBrowserDownloadUrl();

        } catch (IOException e) {
            throw new IllegalStateException("ER", e);
        }

        return result;
    }

    private InputStream getZipInputStream(@NonNull String downloadUrl) {
        final InputStream result;
        try {
            Response<ResponseBody> response = gitHubApiMapper.downloadFileWithDynamicUrlSync(downloadUrl)
                    .execute();
            if (!response.isSuccessful()) {
                throw new IllegalArgumentException("ER");
            }

            if (response.body() == null) {
                throw new IllegalArgumentException("ER");
            }

            result = response.body().byteStream();
        } catch (IOException e) {
            throw new IllegalStateException("ER", e);
        }

        return result;
    }

    /**
     * @param is
     * @return
     * @throws IOException
     */
    @VisibleForTesting
    Optional<DistFileFormatV1> salvageDistFileV1FromInputStream(@NonNull InputStream is) throws IOException {

        DistFileFormatV1 result = null;

        try (ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry entry;
            while (null != (entry = zis.getNextEntry())) {
                if (entry.isDirectory()) {
                    zis.closeEntry();
                    continue;
                }

                if (Paths.get(entry.getName()).compareTo(Path.of(".dist.v1.json")) == 0) {
                    result = new ObjectMapper().readValue(zis.readAllBytes(), DistFileFormatV1.class);
                    zis.closeEntry();
                    break;
                }

                zis.closeEntry();
            }
        }

        return Optional.ofNullable(result);
    }

    /**
     * @param is
     * @param distFileFormatV1
     * @return
     * @throws IOException
     */
    @VisibleForTesting
    Map<Path, byte[]> salvageFilesFromInputStream(@NonNull InputStream is, DistFileFormatV1 distFileFormatV1) throws IOException {
        final Map<Path, byte[]> result = new HashMap<>();

        final Pattern pattern = Pattern.compile(String.join("|", distFileFormatV1.getFilter()));

        try (ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry entry;
            while (null != (entry = zis.getNextEntry())) {
                if (entry.isDirectory()) {
                    zis.closeEntry();
                    continue;
                }

                if (pattern.matcher(entry.getName()).find()) {
                    result.put(Path.of(entry.getName()), zis.readAllBytes());
                    zis.closeEntry();
                }

                zis.closeEntry();
            }
        }

        return result;
    }

    /**
     * @param resources
     * @return 固めたzipファイル
     * @throws IOException
     */
    @VisibleForTesting
    byte[] concreteZip(@NonNull Map<Path, byte[]> resources) throws IOException {
        final byte[] result;

        try (ByteArrayOutputStream bout = new ByteArrayOutputStream(20_000_000); // 20MB
             BufferedOutputStream bos = new BufferedOutputStream(bout);
             ZipOutputStream zos = new ZipOutputStream(bos)
        ) {
            for (Map.Entry<Path, byte[]> item : resources.entrySet()) {
                ZipEntry entry = new ZipEntry(item.getKey().toString());
                zos.putNextEntry(entry);
                zos.write(item.getValue());
                zos.closeEntry();
            }
            zos.finish();
            bos.flush();
            result = bout.toByteArray();
        }

        return result;
    }

    private byte[] salvageFileFromDownloadUrl(@NonNull String downloadUrl) {
        final byte[] result;
        try (InputStream inputStream = getZipInputStream(downloadUrl);
             ZipInputStream zis = new ZipInputStream(inputStream);
             ByteArrayOutputStream bout = new ByteArrayOutputStream(300_000); // 200～300kb
             BufferedOutputStream out = new BufferedOutputStream(bout)) {

            Map<Path, byte[]> resources = salvageFilesFromInputStream(
                    inputStream,
                    salvageDistFileV1FromInputStream(inputStream).orElseGet(() ->
                            DistFileFormatV1
                                    .builder()
                                    .filter(Arrays.asList("Plugin.dll"))
                                    .isArchive(Boolean.TRUE)
                                    .build()
                    )
            );


//            while (null != (entry = zis.getNextEntry())) {
//                byte[] buffer = new byte[2048];
//
//                if (!entry.isDirectory() && Paths.get(entry.getName()).getFileName().toString().equals(
//                        targetFileName
//                )) {
//                    int size;
//                    while (0 < (size = zis.read(buffer))) {
//                        out.write(buffer, 0, size);
//                    }
//                    zis.closeEntry();
//                    break;
//                }
//            }
//            out.flush();
//            result = bout.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return null;
    }

    public byte[] getDllFromAsset(@NonNull String owner,
                                  @NonNull String repoName,
                                  int assetId) {

        return salvageFileFromDownloadUrl(
                getAssetDownloadUrl(owner, repoName, assetId)
        );
    }
}
