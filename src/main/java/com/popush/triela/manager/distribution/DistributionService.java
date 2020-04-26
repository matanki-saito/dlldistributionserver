package com.popush.triela.manager.distribution;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.JsonParseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.popush.triela.common.aws.S3Service;
import com.popush.triela.common.db.ExeDto;
import com.popush.triela.common.db.ExeSelectCondition;
import com.popush.triela.common.db.FileDto;
import com.popush.triela.common.db.FileSelectCondition;
import com.popush.triela.common.exception.ArgumentException;
import com.popush.triela.common.exception.GitHubResourceException;
import com.popush.triela.common.exception.MachineException;
import com.popush.triela.common.exception.OtherSystemException;
import com.popush.triela.common.github.GitHubApiService;
import com.popush.triela.common.github.GitHubReleaseResponse;
import com.popush.triela.common.github.GitHubReposResponse;
import com.popush.triela.db.ExeDao;
import com.popush.triela.db.FileDao;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistributionService {

    private final ExeDao exeDaoMapper;
    private final FileDao fileDaoMapper;
    private final GitHubApiService gitHubApiService;
    private final DistributionProperties properties;
    private final S3Service s3Service;

    /**
     * MD5ハッシュの作成
     *
     * @param source 元の文字列
     * @return ハッシュ後の文字列
     * @throws MachineException exp
     */
    private static String calMd5(byte[] source) throws MachineException {

        final MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new MachineException("Cannot get to md5 message digest instance", e);
        }

        final byte[] md5Bytes = md.digest(source);
        final BigInteger bigInt = new BigInteger(1, md5Bytes);

        return String.format("%032x", bigInt);
    }

    /**
     * ファイルからMD5を生成
     *
     * @param source ファイル
     * @return MD5
     * @throws MachineException exp
     */
    private static String calMd5(@NonNull Path source) throws OtherSystemException {

        final MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new MachineException("Cannot get to md5 message digest instance", e);
        }

        try (DigestInputStream input = new DigestInputStream(Files.newInputStream(source), md)) {
            // ファイルの読み込み
            input.readAllBytes();
        } catch (IOException e) {
            throw new MachineException("file read error", e);
        }

        final byte[] md5Bytes = md.digest();
        final BigInteger bigInt = new BigInteger(1, md5Bytes);
        return String.format("%032x", bigInt);
    }

    private static Optional<String> getExt(Path fileName) {
        if (fileName == null) {
            return Optional.empty();
        }
        int point = fileName.toString().lastIndexOf('.');
        if (point != -1) {
            return Optional.of(fileName.toString().substring(point + 1));
        }

        return Optional.empty();
    }

    /**
     * @param deliverable 配布設定
     * @param assetId     アセットID
     * @throws ArgumentException    引数に問題あり
     * @throws OtherSystemException マシン異常
     */
    private void registerDeliverableFromJson(@NotNull Path deliverable, int assetId)
            throws ArgumentException, OtherSystemException {

        final DistFileFormatV2 jsonData;
        try {
            jsonData = new ObjectMapper().readValue(deliverable.toFile(), DistFileFormatV2.class);
        } catch (JsonParseException | JsonMappingException e) {
            throw new ArgumentException("json parse error", e, deliverable);
        } catch (IOException e) {
            throw new OtherSystemException("json read value error", e);
        }

        fileDaoMapper.upsert(
                FileDto.builder()
                       .assetId(assetId)
                       .data(null)
                       .md5(jsonData.getFileMd5())
                       .dataSize(jsonData.getFileSize())
                       .dataUrl(jsonData.getUrl())
                       .build()
        );
    }

    private void saveDeliverable(@NonNull Path deliverable, int assetId) throws OtherSystemException {

        final long fileSize;
        try {
            fileSize = Files.size(deliverable);
        } catch (IOException e) {
            throw new OtherSystemException("cannot get deliverable size", e);
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
                    FileDto.builder()
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
                throw new OtherSystemException("Cannot read file", e);
            }

            fileDaoMapper.upsert(
                    FileDto.builder()
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
     * @param exeMd5       exeのMD5
     * @param gitHubRepoId github レポジトリ ID
     * @param dllMd5       dllのmd5
     * @param phase        prodかdevか
     * @return あればDLLのデータ
     */
    @Transactional(readOnly = true)
    public Optional<FileDto> getCurrentDistributedDllData(@NotNull String exeMd5,
                                                          int gitHubRepoId,
                                                          @NotNull String dllMd5,
                                                          @NotNull String phase) {
        return fileDaoMapper.list(FileSelectCondition.builder()
                                                     .distributedExeMd5(exeMd5)
                                                     .gitHubRepoId(gitHubRepoId)
                                                     .md5(dllMd5)
                                                     .phase(phase)
                                                     .build())
                            .stream()
                            .findFirst();
    }

    /**
     * ファイルを検索
     *
     * @param gitHubRepoId github レポジトリ ID
     * @param phase        prodかdevか
     * @return あればDLLのデータ
     */
    @Transactional(readOnly = true)
    public Optional<FileDto> getMatchDllData(@NotNull String exeMd5,
                                             int gitHubRepoId,
                                             @NotNull String phase) {
        return fileDaoMapper.list(FileSelectCondition.builder()
                                                     .distributedExeMd5(exeMd5)
                                                     .gitHubRepoId(gitHubRepoId)
                                                     .phase(phase)
                                                     .build())
                            .stream()
                            .findFirst();
    }

    /**
     * ファイルを検索
     *
     * @param gitHubRepoId github レポジトリ ID
     * @param phase        prodかdevか
     * @return あればDLLのデータ
     */
    @Transactional(readOnly = true)
    public Optional<FileDto> getLatestDllData(int gitHubRepoId,
                                              @NotNull String phase) {
        return fileDaoMapper.list(FileSelectCondition.builder()
                                                     .gitHubRepoId(gitHubRepoId)
                                                     .phase(phase)
                                                     .build())
                            .stream()
                            .findFirst();
    }

    /**
     * リリース情報からアセットの一覧を列挙して出力する
     *
     * @param gitHubReposResponse githubの情報
     * @param token               アクセストークン
     * @return アセットの一覧
     * @throws OtherSystemException exp
     */
    List<AssetForm> list(@NonNull GitHubReposResponse gitHubReposResponse, @NonNull String token)
            throws OtherSystemException {

        final List<GitHubReleaseResponse> response = gitHubApiService.getReleasesSync(
                gitHubReposResponse.getOwner().getLogin(),
                gitHubReposResponse.getName(),
                token
        );

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
     * @throws OtherSystemException exp
     */
    @VisibleForTesting
    public Optional<DistFileFormatV1> salvageDistFileV1FromAsset(@NonNull Path assetFile)
            throws OtherSystemException {
        DistFileFormatV1 result = null;

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(assetFile))) {
            ZipEntry entry;
            while (null != (entry = zis.getNextEntry())) {
                if (entry.isDirectory()) {
                    zis.closeEntry();
                    continue;
                }

                if (Paths.get(entry.getName()).endsWith(Path.of(".dist.v1.json")) ||
                    Paths.get(entry.getName()).endsWith(Path.of("dist.v1.json"))) {
                    result = new ObjectMapper().readValue(zis.readAllBytes(), DistFileFormatV1.class);
                    zis.closeEntry();
                    break;
                }

                zis.closeEntry();
            }
        } catch (IOException e) {
            throw new OtherSystemException("OUTER ERR: cannot read asset file.", e);
        }

        return Optional.ofNullable(result);
    }

    /**
     * アセットファイル（Zip）から成果物を作成
     *
     * @param assetFile アセットファイル
     * @return 成果物のパス
     * @throws OtherSystemException exp
     */
    private Path buildDeliverableFromArchive(@NonNull Path assetFile) throws OtherSystemException {

        // distファイルを探す
        final DistFileFormatV1 distInfo;
        distInfo = salvageDistFileV1FromAsset(assetFile).orElseGet(() ->
                                                                           DistFileFormatV1
                                                                                   .builder()
                                                                                   .filter(Collections
                                                                                                   .singletonList(
                                                                                                           "Plugin.dll$")) // default
                                                                                   .isArchive(
                                                                                           Boolean.FALSE) // default
                                                                                   .build()
        );

        // assertファイルからリソースを抽出
        final Map<Path, Path> files;
        files = salvageFilesFromAssetFile(assetFile, distInfo);

        // 成果物を作成
        return deliverable(files, distInfo);
    }

    /**
     * アセットファイルから指定されたファイルと抽出する
     *
     * @param assetFile        アセットファイル
     * @param distFileFormatV1 dist情報
     * @return 抽出されたファイルマップ.
     * @throws OtherSystemException exp
     */
    @VisibleForTesting
    public Map<Path, Path> salvageFilesFromAssetFile(@NonNull Path assetFile,
                                                     @NonNull DistFileFormatV1 distFileFormatV1)
            throws OtherSystemException {
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
     * @throws OtherSystemException exp
     */
    @VisibleForTesting
    public Path concreteZip(@NonNull Map<Path, Path> files) throws OtherSystemException {

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
     * @throws OtherSystemException exp
     */
    private Path deliverable(@NonNull Map<Path, Path> files,
                             @NonNull DistFileFormatV1 distFileFormatV1) throws OtherSystemException {
        // 1つもみつからない。
        if (files.size() == 0) {
            throw new IllegalArgumentException("Not found deliverable.");
        }

        // archiveがfalse
        if (distFileFormatV1.getIsArchive().equals(Boolean.FALSE)) {
            // 1つのみ -> そのまま成果物とする
            if (files.size() == 1) {
                return files.entrySet().stream().findFirst().orElseThrow().getValue();
            } else {
                // 複数あるのにarchiveがfalse -> なにかおかしいが無視して圧縮
                log.info("logic err ? files.size > 1 and archive false ");
            }
        }

        // zipにまとめて返却
        return concreteZip(files);
    }

    /**
     * アセットを永続化する
     *
     * @param owner    Github repo owner
     * @param repoName repository name
     * @param assetId  アセットID
     * @param token    トークン
     * @return 永続化されたらtrue
     * @throws OtherSystemException 引数異常
     */
    private boolean assetPersist(@NonNull String owner,
                                 @NonNull String repoName,
                                 int assetId,
                                 String token) throws OtherSystemException {
        final Optional<FileDto> fileDao = fileDaoMapper.selectByAssetId(assetId);
        if (fileDao.isEmpty()) {

            // gitHubからアセットを取得
            final GitHubApiService.NetworkResource assetFile = gitHubApiService.getDllFromAsset(
                    owner,
                    repoName,
                    assetId,
                    token
            );

            //ファイル形式に応じて処理を変更
            Optional<String> ext = getExt(assetFile.getOriginalFileNamePath());
            if (ext.isEmpty()) {
                throw new GitHubResourceException("not found ext");
            }

            try {
                switch (ext.get()) {
                    case "zip":
                        saveDeliverable(buildDeliverableFromArchive(assetFile.getPath()), assetId);
                        break;
                    case "json":
                        registerDeliverableFromJson(assetFile.getPath(), assetId);
                        break;
                    default:
                        throw new ArgumentException("Not support file type", assetFile);
                }
            } catch (ArgumentException e) {
                throw new GitHubResourceException("bad asset", e);
            }

            return true;
        } else {
            return false;
        }
    }

    /**
     * データを更新する
     *
     * @param exeId2assetId 更新対象となるexeIDとアセットIDのマップ
     * @param owner         レポジトリオーナー
     * @param repoName      レポジトリ名
     * @param repoId        レポジトリID
     * @param token         アクセストークン
     * @throws OtherSystemException 外部異常
     */
    @Transactional
    public void update(@NonNull Map<Integer, Integer> exeId2assetId,
                       @NonNull String owner,
                       @NonNull String repoName,
                       @NonNull Integer repoId,
                       @NonNull String token) throws OtherSystemException {
        for (Map.Entry<Integer, Integer> entry : exeId2assetId.entrySet()) {
            final int exeId = entry.getKey();
            final int assetId = entry.getValue();

            if (assetPersist(owner, repoName, assetId, token)) {
                log.info("persist asset");
            }

            var condition = ExeSelectCondition
                    .builder()
                    .id(exeId)
                    .gitHubRepoId(repoId)
                    .build();

            final List<ExeDto> exeDaoList = exeDaoMapper.list(condition);

            if (exeDaoList.size() != 1) {
                throw new OtherSystemException("exeDaoList is not one. Maybe db state error.");
            }

            exeDaoMapper.update(
                    condition,
                    ExeDto
                            .builder()
                            .distributionAssetId(assetId)
                            .build()
            );
        }
    }

}
