package com.popush.triela.Manager.distribution;

import com.popush.triela.common.AWS.S3Service;
import com.popush.triela.common.DB.*;
import com.popush.triela.common.Exception.NotModifiedException;
import com.popush.triela.common.github.GitHubApiService;
import com.popush.triela.common.github.GitHubReleaseResponse;
import com.popush.triela.common.github.GitHubReposResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DistributionService {

    private final ExeDaoMapper exeDaoMapper;
    private final FileDaoMapper fileDaoMapper;
    private final GitHubApiService gitHubApiService;
    private final S3Service s3Service;
    private final DistributionProperties properties;

    /**
     * MD5ハッシュの作成
     *
     * @param source 元の文字列
     * @return ハッシュ後の文字列
     */
    private static String calMd5(byte[] source) {

        final MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("message digest", e);
        }
        final byte[] md5Bytes = md.digest(source);
        final BigInteger bigInt = new BigInteger(1, md5Bytes);

        return String.format("%032x", bigInt);
    }

    /**
     * ファイルを検索
     *
     * @param condition 条件
     * @return データ
     */
    //@Cacheable("dllCache") うまく動作してるか心配だったのでコメントアウトしてる
    @Transactional(readOnly = true)
    public Optional<FileDao> getDllData(@NonNull FileSelectCondition condition) throws NotModifiedException {
        final List<FileDao> fileDaoList = fileDaoMapper.list(condition);

        if (fileDaoList.size() != 1) {
            throw new NotModifiedException();
        }

        return Optional.of(fileDaoList.get(0));
    }

    List<AssetForm> list(@NonNull GitHubReposResponse gitHubReposResponse) {
        final List<GitHubReleaseResponse> response = gitHubApiService.getReleasesSync(
                gitHubReposResponse.getOwner().getLogin(),
                gitHubReposResponse.getName()
        );

        return response.stream()
                .filter(elem -> !elem.getAssets().isEmpty())
                .map(elem -> AssetForm.builder()
                        .name(elem.getName())
                        .url(elem.getHtmlUrl())
                        .assetId(Integer.toString(elem.getAssets().get(0).getId()))
                        .build()
                ).collect(Collectors.toList());
    }

    @Transactional
    void update(
            @NonNull Map<String, Integer> exeHash2assetId,
            @NonNull GitHubReposResponse gitHubReposResponse) {
        for (Map.Entry<String, Integer> entry : exeHash2assetId.entrySet()) {
            final String exeMd5 = entry.getKey();
            final int assetId = entry.getValue();

            final FileDao fileDao = fileDaoMapper.selectByAssetId(assetId);
            if (Objects.isNull(fileDao)) {
                // byteでやり取りするのは色々問題あるのでファイルに落とし込むようにする。
                final byte[] data = gitHubApiService.getDllFromAsset(
                        gitHubReposResponse.getOwner().getLogin(),
                        gitHubReposResponse.getName(),
                        assetId
                );

                // 1MB超えていたらS3に保存
                //if (data.length > 1_000_000) {
                final String key = UUID.randomUUID().toString();
                s3Service.upload(data, properties.getS3BucketName(), key);

                String dataUrl = String.format("%s://%s/%s",
                        "https",
                        properties.getCloudFrontDomainName(),
                        key
                );
                //}

                fileDaoMapper.upsert(
                        FileDao.builder()
                                .assetId(assetId)
                                .data(data)
                                .md5(calMd5(data))
                                .dataSize(data.length)
                                .dataUrl(dataUrl)
                                .build()
                );
            }

            final List<ExeDao> exeDaoList = exeDaoMapper.list(ExeSelectCondition
                    .builder()
                    .md5(exeMd5)
                    .gitHubRepoId(gitHubReposResponse.getId())
                    .build()
            );

            if (exeDaoList.size() != 1) {
                throw new IllegalArgumentException("a");
            }

            exeDaoList.get(0).setDistributionAssetId(assetId);
            exeDaoMapper.upsert(exeDaoList.get(0));
        }
    }

}
