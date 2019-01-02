package com.popush.triela.api.distribution;

import com.popush.triela.Manager.distribution.DistributionService;
import com.popush.triela.api.TrielaApiV1Controller;
import com.popush.triela.common.DB.FileSelectCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class DistributionApiController extends TrielaApiV1Controller {

    private final DistributionService distributionMgrService;

    @GetMapping("/distribution/test")
    public String test() {
        return "abc";
    }

    @GetMapping("/distribution/{gitHubRepoId}/{exe_md5}")
    public HttpEntity<byte[]> fileGet(@PathVariable(value = "gitHubRepoId") int gitHubRepoId,
                                      @PathVariable(value = "exe_md5") String exeMd5,
                                      @RequestParam(value = "dll_md5", required = false) String dllMd5) {

        final Optional<byte[]> result = distributionMgrService.getDllData(FileSelectCondition.builder()
                .distributedExeMd5(exeMd5)
                .gitHubRepoId(gitHubRepoId)
                .md5(dllMd5)
                .build());

        if (result.isEmpty()) {
            throw new IllegalArgumentException("bad request");
        }

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentLength(result.get().length);

        return new HttpEntity<>(result.get(), headers);
    }
}
