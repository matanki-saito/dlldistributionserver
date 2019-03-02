package com.popush.triela.api.distribution;

import com.popush.triela.Manager.distribution.DistributionService;
import com.popush.triela.api.TrielaApiV1Controller;
import com.popush.triela.common.DB.FileDao;
import com.popush.triela.common.DB.FileSelectCondition;
import com.popush.triela.common.Exception.NotModifiedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@Slf4j
public class DistributionApiController extends TrielaApiV1Controller {

    private final DistributionService distributionMgrService;

    @GetMapping("/distribution/{gitHubRepoId}/{exe_md5}")
    public ResponseEntity fileGet(@PathVariable(value = "gitHubRepoId") int gitHubRepoId,
                                  @PathVariable(value = "exe_md5") String exeMd5,
                                  @RequestParam(value = "dll_md5", required = false) String dllMd5,
                                  @RequestParam(value = "phase", required = false, defaultValue = "prod") String phase) throws NotModifiedException {

        // リクエストに一致するエントリの取得
        final Optional<FileDao> result = distributionMgrService.getDllData(FileSelectCondition.builder()
                .distributedExeMd5(exeMd5)
                .gitHubRepoId(gitHubRepoId)
                .md5(dllMd5)
                .phase(phase)
                .build());

        // 存在しない
        if (result.isEmpty()) {
            throw new IllegalStateException();
        }
        final FileDao fileDao = result.get();

        Object responseBody;
        HttpStatus status;
        final HttpHeaders headers = new HttpHeaders();

        if (fileDao.getDataUrl() == null) {
            if (fileDao.getData().length <= 0) {
                // 不明
                throw new IllegalStateException("???");
            } else {
                // dataあり
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentLength(fileDao.getDataSize());
                status = HttpStatus.OK;
                responseBody = fileDao.getData();
            }
        } else {
            // redirect URL
            headers.setLocation(URI.create(fileDao.getDataUrl()));
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
