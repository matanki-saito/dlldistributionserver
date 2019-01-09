package com.popush.triela.api.distribution;

import com.popush.triela.Manager.distribution.DistributionService;
import com.popush.triela.api.TrielaApiV1Controller;
import com.popush.triela.common.DB.FileSelectCondition;
import com.popush.triela.common.Exception.NotModifiedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@Slf4j
public class DistributionApiController extends TrielaApiV1Controller {

    private final DistributionService distributionMgrService;

    @GetMapping("/distribution/error")
    public String error() {
        log.error("error dayo");
        return "error";
    }

    @GetMapping("/distribution/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok().cacheControl(
                CacheControl.maxAge(15, TimeUnit.MINUTES))
                .body("test");
    }

    @GetMapping("/distribution/{gitHubRepoId}/{exe_md5}")
    public ResponseEntity<byte[]> fileGet(@PathVariable(value = "gitHubRepoId") int gitHubRepoId,
                                          @PathVariable(value = "exe_md5") String exeMd5,
                                          @RequestParam(value = "dll_md5", required = false) String dllMd5) throws NotModifiedException {

        final Optional<byte[]> result = distributionMgrService.getDllData(FileSelectCondition.builder()
                .distributedExeMd5(exeMd5)
                .gitHubRepoId(gitHubRepoId)
                .md5(dllMd5)
                .build());

        if (result.isEmpty()) {
            throw new IllegalStateException();
        }

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentLength(result.get().length);

        return ResponseEntity
                .ok()
                .cacheControl(CacheControl.maxAge(15, TimeUnit.MINUTES))
                .headers(headers)
                .body(result.get());
    }
}
