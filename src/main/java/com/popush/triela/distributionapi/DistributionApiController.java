package com.popush.triela.distributionapi;

import com.popush.triela.common.DB.FileSelectCondition;
import com.popush.triela.distributionmanager.DistributionMgrService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DistributionApiController {

    private final DistributionMgrService distributionMgrService;

    @GetMapping("/test")
    public String test() {
        return "test";
    }

    @GetMapping("/distribution/{product}/{exe_md5}")
    public HttpEntity<byte[]> fileGet(@PathVariable(value = "product") String product,
                                      @PathVariable(value = "exe_md5") String exeMd5,
                                      @RequestParam(value = "dll_md5", required = false) String dllMd5) {

        final Optional<byte[]> result = distributionMgrService.getDllData(FileSelectCondition.builder()
                .distributedExeMd5(exeMd5)
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
