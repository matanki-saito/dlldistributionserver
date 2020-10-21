package com.popush.triela.api.distribution;


import java.util.Optional;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.popush.triela.common.db.FileDto;
import com.popush.triela.common.exception.NotModifiedException;
import com.popush.triela.manager.distribution.DistributionService;

@ExtendWith(SoftAssertionsExtension.class)
@ExtendWith(MockitoExtension.class)
class DistributionApiControllerTest {
    @InjectMocks
    private DistributionApiController controller;

    @Mock
    private DistributionService service;

    @Test
    void fileGetNoUpdate(SoftAssertions softly) throws Exception {
        // リクエスト
        final int gitHubRepoId = 10000;
        final String exeMd5 = "aaaaaaaaaaaaaaaaaaaaa";
        final String dllMd5 = "111111111111111111111";
        final String phase = "prod";

        // リクエストされたものは現在配信中の物
        when(service.getCurrentDistributedDllData(exeMd5,
                                                  gitHubRepoId,
                                                  dllMd5,
                                                  phase))
                .thenReturn(Optional.of(FileDto.builder().build()));

        // 更新してもらう必要はない
        Assertions.assertThrows(NotModifiedException.class, () -> {
            controller.searchOptimalFileDto(gitHubRepoId,
                                            exeMd5,
                                            dllMd5,
                                            phase);
        });

        verify(service, times(1))
                .getCurrentDistributedDllData(exeMd5, gitHubRepoId, dllMd5, phase);
        verify(service, times(0))
                .getMatchDllData(exeMd5, gitHubRepoId, phase);
        verify(service, times(0))
                .getLatestDllData(gitHubRepoId, phase);
    }

    @Test
    void fileGetUpdate(SoftAssertions softly) throws Exception {
        // リクエスト
        final int gitHubRepoId = 10000;
        final String exeMd5 = "aaaaaaaaaaaaaaaaaaaaa";
        final String dllMd5 = "111111111111111111111";
        final String phase = "prod";

        // リクエストされたものは現在配信中の物とは異なる（見つからない）
        when(service.getCurrentDistributedDllData(exeMd5,
                                                  gitHubRepoId,
                                                  dllMd5,
                                                  phase))
                .thenReturn(Optional.empty());

        // DLLが古かった（新しいのが見つかる）
        when(service.getMatchDllData(exeMd5,
                                     gitHubRepoId,
                                     phase))
                .thenReturn(Optional.of(FileDto.builder()
                                               .assetId(555)
                                               .build()));

        // 最新のものをダウンロードしてもらうようにする
        final var result = controller.searchOptimalFileDto(gitHubRepoId,
                                                           exeMd5,
                                                           dllMd5,
                                                           phase);
        softly.assertThat(result.getAssetId()).isEqualTo(555);

        verify(service, times(1))
                .getCurrentDistributedDllData(exeMd5, gitHubRepoId, dllMd5, phase);
        verify(service, times(1))
                .getMatchDllData(exeMd5, gitHubRepoId, phase);
        verify(service, times(0))
                .getLatestDllData(gitHubRepoId, phase);
    }

    @Test
    void fileGetLatest(SoftAssertions softly) throws Exception {
        // リクエスト
        final int gitHubRepoId = 10000;
        final String exeMd5 = "aaaaaaaaaaaaaaaaaaaaa";
        final String dllMd5 = "111111111111111111111";
        final String phase = "prod";

        // リクエストされたものは現在配信中の物とは異なる（見つからない）
        when(service.getCurrentDistributedDllData(exeMd5,
                                                  gitHubRepoId,
                                                  dllMd5,
                                                  phase))
                .thenReturn(Optional.empty());

        // EXEが未登録（見つからない）
        when(service.getMatchDllData(exeMd5,
                                     gitHubRepoId,
                                     phase))
                .thenReturn(Optional.empty());

        // 登録してあるものの中で最新のものを抽出
        when(service.getLatestDllData(gitHubRepoId, phase))
                .thenReturn(Optional.of(FileDto.builder()
                                               .md5("111111111111111111112")
                                               .assetId(555)
                                               .build()));

        // 最新のものはリクエストと異なる
        final var result = controller.searchOptimalFileDto(gitHubRepoId,
                                                           exeMd5,
                                                           dllMd5,
                                                           phase);
        softly.assertThat(result.getAssetId()).isEqualTo(555);

        verify(service, times(1))
                .getCurrentDistributedDllData(exeMd5, gitHubRepoId, dllMd5, phase);
        verify(service, times(1))
                .getMatchDllData(exeMd5, gitHubRepoId, phase);
        verify(service, times(1))
                .getLatestDllData(gitHubRepoId, phase);
    }

    @Test
    void fileGetLatestNoUpdate(SoftAssertions softly) throws Exception {
        // リクエスト
        final int gitHubRepoId = 10000;
        final String exeMd5 = "aaaaaaaaaaaaaaaaaaaaa";
        final String dllMd5 = "111111111111111111111";
        final String phase = "prod";

        // リクエストされたものは現在配信中の物とは異なる（見つからない）
        when(service.getCurrentDistributedDllData(exeMd5,
                                                  gitHubRepoId,
                                                  dllMd5,
                                                  phase))
                .thenReturn(Optional.empty());

        // EXEが未登録（見つからない）
        when(service.getMatchDllData(exeMd5,
                                     gitHubRepoId,
                                     phase))
                .thenReturn(Optional.empty());

        // 登録してあるものの中で最新のものを抽出
        when(service.getLatestDllData(gitHubRepoId, phase))
                .thenReturn(Optional.of(FileDto.builder()
                                               .md5(dllMd5)
                                               .assetId(555)
                                               .build()));

        // 最新のものはリクエストとおなじなので更新の必要はなし
        Assertions.assertThrows(NotModifiedException.class, () -> {
            controller.searchOptimalFileDto(gitHubRepoId,
                                            exeMd5,
                                            dllMd5,
                                            phase);
        });

        verify(service, times(1))
                .getCurrentDistributedDllData(exeMd5, gitHubRepoId, dllMd5, phase);
        verify(service, times(1))
                .getMatchDllData(exeMd5, gitHubRepoId, phase);
        verify(service, times(1))
                .getLatestDllData(gitHubRepoId, phase);
    }
}
