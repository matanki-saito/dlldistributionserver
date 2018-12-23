package com.popush.triela.distributionmanager;

import com.popush.triela.common.DB.*;
import com.popush.triela.github.GitHubApiService;
import com.popush.triela.github.GitHubReleaseResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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
public class DistributionMgrService {
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
    public Optional<byte[]> getDllData(@NonNull FileSelectCondition condition) {
        final FileDao dao = fileDaoMapper.selectByExeMd5(condition);
        return dao == null ? Optional.empty() : Optional.of(dao.getData());
    }

    List<AssetForm> list() {
        final List<GitHubReleaseResponse> response = gitHubApiService.getReleasesSync(DLL_OWNER, "eu4dll");

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
            @NonNull String repoName) {
        for (Map.Entry<String, Integer> entry : exeHash2assetId.entrySet()) {
            final String exeMd5 = entry.getKey();
            final int assetId = entry.getValue();

            final FileDao fileDao = fileDaoMapper.selectByAssetId(assetId);
            if (Objects.isNull(fileDao)) {
                final byte[] data = gitHubApiService.getDllFromAsset(
                        DLL_OWNER,
                        repoName,
                        assetId,
                        "Plugin.dll"
                );
                fileDaoMapper.upsert(
                        FileDao.builder().assetId(assetId).data(data).md5(calMd5(data)).build()
                );
            }

            final ExeDao exeDao = exeDaoMapper.selectByMd5(exeMd5);
            if (Objects.isNull(exeDao)) {
                throw new IllegalArgumentException("a");
            }

            exeDao.setDistributionAssetId(assetId);
            exeDaoMapper.upsert(exeDao);
        }
    }
}
