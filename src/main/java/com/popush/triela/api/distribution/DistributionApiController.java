package com.popush.triela.api.distribution;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.google.common.annotations.VisibleForTesting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.popush.triela.api.TrielaApiV1Controller;
import com.popush.triela.common.db.FileDto;
import com.popush.triela.common.exception.NotModifiedException;
import com.popush.triela.manager.distribution.DistributionService;

@RestController
@RequiredArgsConstructor
@Slf4j
public class DistributionApiController extends TrielaApiV1Controller {

    private final DistributionService distributionMgrService;


    @VisibleForTesting
    FileDto searchOptimalFileDto(int gitHubRepoId,
                                 String exeMd5,
                                 String dllMd5,
                                 String phase) throws NotModifiedException {
        // 現在配信中のデータを取得
        final Optional<FileDto> result = distributionMgrService.getCurrentDistributedDllData(exeMd5,
                                                                                             gitHubRepoId,
                                                                                             dllMd5,
                                                                                             phase);

        // 存在していればさらに更新する必要はないので、405を返却するために例外を発生させる
        if (result.isPresent()) {
            throw new NotModifiedException();
        }

        // 必要なDLLを取得
        final FileDto fileDao;

        // 現在のdll状態を無視して検索
        var match = distributionMgrService.getMatchDllData(exeMd5, gitHubRepoId, phase);
        if (match.isPresent()) {
            fileDao = match.get();
        } else {
            // exeがまだ未登録だと判断して、最新のversionを取得
            var latest = distributionMgrService.getLatestDllData(gitHubRepoId, phase);

            // それもない場合はリクエストに問題がある（存在しないrepoId）としてエラー
            fileDao = latest.orElseThrow(IllegalArgumentException::new);

            // ただしそれがすでに最新であれば更新の必要はない
            if (fileDao.getMd5().equals(dllMd5)) {
                throw new NotModifiedException();
            }
        }

        return fileDao;
    }

    @GetMapping("/distribution/{gitHubRepoId}/{exe_md5}")
    public ResponseEntity<Object> fileGet(@PathVariable(value = "gitHubRepoId") int gitHubRepoId,
                                          @PathVariable(value = "exe_md5") String exeMd5,
                                          @RequestParam(value = "dll_md5", required = false, defaultValue = "") String dllMd5,
                                          @RequestParam(value = "phase", required = false, defaultValue = "prod") String phase)
            throws NotModifiedException {


        final FileDto fileDto = searchOptimalFileDto(gitHubRepoId, exeMd5, dllMd5, phase);

        Object responseBody;
        HttpStatus status;
        final HttpHeaders headers = new HttpHeaders();

        if (fileDto.getDataUrl() == null) {
            if (fileDto.getData().length <= 0) {
                // 不明
                throw new IllegalStateException("???");
            } else {
                // dataあり
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentLength(fileDto.getDataSize());
                status = HttpStatus.OK;
                responseBody = fileDto.getData();
            }
        } else {
            // redirect URL
            headers.setLocation(URI.create(fileDto.getDataUrl()));
            status = HttpStatus.FOUND;
            responseBody = "redirect";
        }

        return ResponseEntity
                .status(status)
                .cacheControl(CacheControl.maxAge(15, TimeUnit.MINUTES))
                .headers(headers)
                .body(responseBody);

    }
}
