package com.popush.triela.Manager.distribution;

import com.popush.triela.common.DB.*;
import com.popush.triela.common.github.GitHubApiService;
import com.popush.triela.common.github.GitHubReleaseResponse;
import com.popush.triela.common.github.GitHubReposResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DistributionService {
    private static final String DLL_OWNER = "matanki-saito";
    private final ExeDaoMapper exeDaoMapper;
    private final FileDaoMapper fileDaoMapper;
    private final GitHubApiService gitHubApiService;

    /**
     * MD5ハッシュの作成
     *
     * @param source 元の文字列
     * @return ハッシュ後の文字列
     */
    private static String calMd5(byte[] source) {

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("message digest", e);
        }
        byte[] md5_bytes = md.digest(source);
        BigInteger big_int = new BigInteger(1, md5_bytes);

        return String.format("%032x", big_int);
    }

    /**
     * ファイルを検索
     *
     * @param condition 条件
     * @return データ
     */
    @Cacheable("dllCache")
    public Optional<byte[]> getDllData(@NonNull FileSelectCondition condition) {
        final List<FileDao> fileDaoList = fileDaoMapper.list(condition);

        if (fileDaoList.size() != 1) {
            throw new IllegalStateException("1");
        }

        return Optional.of(fileDaoList.get(0).getData());
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

    void update(
            @NonNull Map<String, Integer> exeHash2assetId,
            @NonNull GitHubReposResponse gitHubReposResponse) {
        for (Map.Entry<String, Integer> entry : exeHash2assetId.entrySet()) {
            final String exeMd5 = entry.getKey();
            final int assetId = entry.getValue();

            final FileDao fileDao = fileDaoMapper.selectByAssetId(assetId);
            if (Objects.isNull(fileDao)) {
                final byte[] data = gitHubApiService.getDllFromAsset(
                        gitHubReposResponse.getOwner().getLogin(),
                        gitHubReposResponse.getName(),
                        assetId,
                        "Plugin.dll"
                );
                fileDaoMapper.upsert(
                        FileDao.builder().assetId(assetId).data(data).md5(calMd5(data)).build()
                );
            }

            final List<ExeDao> exeDaoList = exeDaoMapper.list(ExeSelectCondition
                    .builder()
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
