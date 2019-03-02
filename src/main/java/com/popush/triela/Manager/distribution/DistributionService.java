package com.popush.triela.Manager.distribution;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.popush.triela.common.AWS.S3Service;
import com.popush.triela.common.DB.*;
import com.popush.triela.common.Exception.GitHubException;
import com.popush.triela.common.Exception.MachineException;
import com.popush.triela.common.Exception.NotModifiedException;
import com.popush.triela.common.github.GitHubApiService;
import com.popush.triela.common.github.GitHubReleaseResponse;
import com.popush.triela.common.github.GitHubReposResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistributionService {

    private final ExeDaoMapper exeDaoMapper;
    private final FileDaoMapper fileDaoMapper;
    private final GitHubApiService gitHubApiService;
    private final DistributionProperties properties;
    private final S3Service s3Service;

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

    private static String calMd5(@NonNull Path source) throws MachineException {

        final MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("message digest", e);
        }

        try (DigestInputStream input = new DigestInputStream(Files.newInputStream(source), md)) {
            // ファイルの読み込み
            while (input.read() != -1) {
            }
        } catch (IOException e) {
            throw new MachineException("Error", e);
        }

        final byte[] md5Bytes = md.digest();
        final BigInteger bigInt = new BigInteger(1, md5Bytes);
        return String.format("%032x", bigInt);
    }

    private void saveDeliverable(@NonNull Path deliverable, int assetId) throws MachineException {

        final long fileSize;
        try {
            fileSize = Files.size(deliverable);
        } catch (IOException e) {
            throw new MachineException("cannot get deliverable size", e);
        }

        // 1MB超えていたらS3に保存
        if (fileSize > 1_000_000) {
            final String key = UUID.randomUUID().toString();
            s3Service.upload(deliverable, properties.getS3BucketName(), key);

            final String dataUrl = String.format("%s://%s/%s",
                    "https",
                    properties.getCloudFrontDomainName(),
                    key
            );

            fileDaoMapper.upsert(
                    FileDao.builder()
                            .assetId(assetId)
                            .data(null)
                            .md5(calMd5(deliverable))
                            .dataSize(fileSize)
                            .dataUrl(dataUrl)
                            .build()
            );
        } else {
            byte[] allDataBytes;
            try {
                allDataBytes = Files.readAllBytes(deliverable);
            } catch (IOException e) {
                throw new MachineException("Cannot read file", e);
            }

            fileDaoMapper.upsert(
                    FileDao.builder()
                            .assetId(assetId)
                            .data(allDataBytes)
                            .md5(calMd5(allDataBytes))
                            .dataSize(allDataBytes.length)
                            .dataUrl(null)
                            .build()
            );
        }
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

        final List<GitHubReleaseResponse> response;
        try {
            response = gitHubApiService.getReleasesSync(
                    gitHubReposResponse.getOwner().getLogin(),
                    gitHubReposResponse.getName()
            );
        } catch (GitHubException e) {
            throw new IllegalStateException("Cannot get release list.", e);
        }

        return response.stream()
                .filter(elem -> !elem.getAssets().isEmpty())
                .map(elem -> AssetForm.builder()
                        .draft(elem.getDraft())
                        .preRelease(elem.getPreRelease())
                        .name(elem.getName())
                        .url(elem.getHtmlUrl())
                        .assetId(Integer.toString(elem.getAssets().get(0).getId()))
                        .build()
                ).collect(Collectors.toList());
    }

    /**
     * アセットファイルから.distファイルを見つけ出す。ない場合もあり得る。
     *
     * @param assetFile アセットファイルのパス
     * @return dist情報
     * @throws MachineException exp
     */
    @VisibleForTesting
    public Optional<DistFileFormatV1> salvageDistFileV1FromAsset(@NonNull Path assetFile) throws MachineException {
        DistFileFormatV1 result = null;

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(assetFile))) {
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
        } catch (IOException e) {
            throw new MachineException("OUTER ERR: cannot read asset file.", e);
        }

        return Optional.ofNullable(result);
    }

    /**
     * アセットファイルから成果物を作成
     *
     * @param assetFile アセットファイル
     * @return 成果物のパス
     */
    private Path buildDeliverable(@NonNull Path assetFile) {
        // distファイルを探す
        final DistFileFormatV1 distInfo;
        try {
            distInfo = salvageDistFileV1FromAsset(assetFile).orElseGet(() ->
                    DistFileFormatV1
                            .builder()
                            .filter(Collections.singletonList("Plugin.dll$")) // default
                            .isArchive(Boolean.FALSE) // default
                            .build()
            );
        } catch (MachineException e) {
            throw new IllegalStateException("Salvage dist exception.", e);
        }

        // assertファイルからリソースを抽出
        final Map<Path, Path> files;
        try {
            files = salvageFilesFromAssetFile(assetFile, distInfo);
        } catch (MachineException e) {
            throw new IllegalStateException("Salvage file exception.", e);
        }

        // 成果物を作成
        return deliverable(files, distInfo);
    }

    /**
     * アセットファイルから指定されたファイルと抽出する
     *
     * @param assetFile        アセットファイル
     * @param distFileFormatV1 dist情報
     * @return 抽出されたファイルマップ.
     * @throws MachineException exp
     */
    @VisibleForTesting
    public Map<Path, Path> salvageFilesFromAssetFile(@NonNull Path assetFile,
                                                     @NonNull DistFileFormatV1 distFileFormatV1) throws MachineException {
        final Map<Path, Path> result = new HashMap<>();

        final Pattern pattern = Pattern.compile(String.join("|", distFileFormatV1.getFilter()));

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(assetFile))) {
            ZipEntry entry;
            while (null != (entry = zis.getNextEntry())) {
                if (entry.isDirectory()) {
                    zis.closeEntry();
                    continue;
                }

                if (pattern.matcher(entry.getName()).find()) {
                    final Path destPath = Files.createTempFile("triela_salvaged_file", ".tmp");
                    Files.copy(zis, destPath, StandardCopyOption.REPLACE_EXISTING);
                    result.put(Path.of(entry.getName()), destPath);
                    zis.closeEntry();
                }

                zis.closeEntry();
            }
        } catch (IOException e) {
            throw new MachineException("Cannot read asset file.", e);
        }

        return result;
    }

    /**
     * 抽出したファイルをzipに固める
     *
     * @param files 抽出したファイル
     * @return 成果物のzip
     * @throws MachineException exp
     */
    @VisibleForTesting
    public Path concreteZip(@NonNull Map<Path, Path> files) throws MachineException {

        // 一時ファイルの場所
        final Path result;
        try {
            result = Files.createTempFile("triela_concreatezip", ".tmp");
        } catch (IOException e) {
            throw new MachineException("Cannot create temp file.", e);
        }

        //
        try (FileOutputStream bout = new FileOutputStream(result.toFile()); // 20MB
             BufferedOutputStream bos = new BufferedOutputStream(bout);
             ZipOutputStream zos = new ZipOutputStream(bos)
        ) {
            for (Map.Entry<Path, Path> item : files.entrySet()) {
                ZipEntry entry = new ZipEntry(item.getKey().toString());
                zos.putNextEntry(entry);

                // コピー
                Files.copy(item.getValue(), zos);

                zos.closeEntry();
            }
            zos.finish();
            bos.flush();
            bout.flush();
        } catch (IOException e) {
            throw new MachineException("Cannot write a temp file.", e);
        }

        return result;
    }

    /**
     * 成果物を返却する
     *
     * @param files            抽出したファイル
     * @param distFileFormatV1 dist情報
     * @return 成果物のパス
     */
    private Path deliverable(@NonNull Map<Path, Path> files,
                             @NonNull DistFileFormatV1 distFileFormatV1) {
        // 1つもみつからない。
        if (files.size() == 0) {
            throw new IllegalArgumentException("Not found deliverable.");
        }

        // archiveがfalse
        if (!distFileFormatV1.getIsArchive()) {
            // 1つのみ -> そのまま成果物とする
            if (files.size() == 1) {
                return files.entrySet().stream().findFirst().get().getValue();
            } else {
                // 複数あるのにarchiveがfalse -> なにかおかしいが無視して圧縮
                log.info("logic err ? files.size > 1 and archive false ");
            }
        }

        // zipにまとめて返却
        try {
            return concreteZip(files);
        } catch (MachineException e) {
            throw new IllegalStateException("");
        }
    }

    @Transactional
    void update(
            @NonNull Map<Integer, Integer> exeId2assetId,
            @NonNull GitHubReposResponse gitHubReposResponse) {
        for (Map.Entry<Integer, Integer> entry : exeId2assetId.entrySet()) {
            final int exeId = entry.getKey();
            final int assetId = entry.getValue();

            final FileDao fileDao = fileDaoMapper.selectByAssetId(assetId);
            if (Objects.isNull(fileDao)) {
                final Path assetFile = gitHubApiService.getDllFromAsset(
                        gitHubReposResponse.getOwner().getLogin(),
                        gitHubReposResponse.getName(),
                        assetId
                );

                final Path deliverable = buildDeliverable(assetFile);

                try {
                    saveDeliverable(deliverable, assetId);
                } catch (MachineException e) {
                    throw new IllegalStateException(e);
                }
            }

            final List<ExeDao> exeDaoList = exeDaoMapper.list(ExeSelectCondition
                    .builder()
                    .id(exeId)
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
