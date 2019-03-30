package com.popush.triela.manager.exe;

import com.popush.triela.common.db.ExeDto;
import com.popush.triela.common.db.ExeSelectCondition;
import com.popush.triela.common.github.GitHubApiService;
import com.popush.triela.db.ExeDao;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ExeService {
    final ExeDao exeDaoMapper;
    final GitHubApiService apiService;

    @Transactional
    void save(@NonNull ExeForm form, int gitHubRepoId) {
        exeDaoMapper.upsert(ExeDto.builder()
                .gitHubRepoId(gitHubRepoId)
                .md5(form.getMd5())
                .version(form.getVersion())
                .description(form.getDescription())
                .phase(form.getPhase().isBlank() ? "prod" : form.getPhase())
                .build()
        );
    }

    @Transactional(readOnly = true)
    public List<ExeForm> list(int gitHubRepoId) {
        return exeDaoMapper.list(ExeSelectCondition.builder().gitHubRepoId(gitHubRepoId).build()).stream().map(
                elem -> ExeForm
                        .builder()
                        .Id(elem.getId())
                        .description(elem.getDescription())
                        .md5(elem.getMd5())
                        .version(elem.getVersion())
                        .distributionAssetId(elem.getDistributionAssetId())
                        .phase(elem.getPhase())
                        .build()
        ).sorted().collect(Collectors.toList());
    }
}