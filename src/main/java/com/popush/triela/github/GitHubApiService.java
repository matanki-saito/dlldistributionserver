package com.popush.triela.github;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import okhttp3.ResponseBody;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
public class GitHubApiService {
    final GitHubApiMapper gitHubApiMapper;

    public List<GitHubReleaseResponse> getReleasesSync(@NonNull String owner,
                                                       @NonNull String repoName){
        final Call<List<GitHubReleaseResponse>> request = gitHubApiMapper.releases(owner,repoName);

        final List<GitHubReleaseResponse> result;
        try {
            final Response<List<GitHubReleaseResponse>> response = request.execute();
            if(!response.isSuccessful()) throw new IllegalStateException("not success");
            result = response.body();

        } catch (IOException e) {
            throw new IllegalStateException("ER",e);
        }

        return result;
    }

    private String getAssetDonloadUrl(@NonNull String owner,
                                      @NonNull String repoName,
                                      int assetId){
        final String result;

        final Call<GitHubAssetResponse> request = gitHubApiMapper.asset(
                owner,
                repoName,
                assetId
        );
        try{
            Response<GitHubAssetResponse> response = request.execute();
            if(!response.isSuccessful()){
                throw new IllegalStateException("not success");
            }
            if(response.body() == null){
                throw new IllegalArgumentException("not success");
            }
            result = response.body().getBrowserDownloadUrl();

        }catch (IOException e){
            throw new IllegalStateException("ER",e);
        }

        return result;
    }

    private InputStream getZipInputStream(@NonNull String downloadUrl){
        final InputStream result;
        try {
            Response<ResponseBody> response = gitHubApiMapper.downloadFileWithDynamicUrlSync(downloadUrl).execute();
            if(!response.isSuccessful()){
                throw new IllegalArgumentException("ER");
            }

            if(response.body() == null){
                throw new IllegalArgumentException("ER");
            }

            result = response.body().byteStream();
        } catch (IOException e) {
            throw new IllegalStateException("ER",e);
        }

        return result;
    }

    private byte[] salvageFileFromZipInputStream(@NonNull String downloadUrl,
                                                 @NonNull String targetFileName){
        final byte[] result;
        try(InputStream inputStream = getZipInputStream(downloadUrl);
            ZipInputStream zis = new ZipInputStream(inputStream);
            ByteArrayOutputStream bout = new ByteArrayOutputStream(300_000); // 200～300kb
            BufferedOutputStream out = new BufferedOutputStream(bout)) {
            ZipEntry entry;
            while(null != (entry = zis.getNextEntry())){

                byte[] buffer = new byte[2048];

                if(!entry.isDirectory() && Paths.get(entry.getName()).getFileName().toString().equals(targetFileName)) {
                    int size;
                    while (0 < (size = zis.read(buffer))) {
                        out.write(buffer, 0, size);
                    }
                    zis.closeEntry();
                }
            }
            out.flush();
            result = bout.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException();
        }

        return result;
    }

    public byte[] getDllFromAsset(@NonNull String owner,
                                  @NonNull String repoName,
                                  int assetId,
                                  @NonNull String targetFileName){

        return salvageFileFromZipInputStream(
                getAssetDonloadUrl(owner,repoName,assetId),
                targetFileName
        );
    }
}
