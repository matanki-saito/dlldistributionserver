package com.popush.triela.Manager.exe;

import com.popush.triela.common.DB.ExeDao;
import com.popush.triela.common.DB.ExeDaoMapper;
import com.popush.triela.common.DB.ExeSelectCondition;
import com.popush.triela.common.github.GitHubApiService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ExeService {
    final ExeDaoMapper exeDaoMapper;
    final GitHubApiService apiService;

    @Transactional
    void save(@NonNull ExeForm form, int gitHubRepoId) {
        exeDaoMapper.upsert(ExeDao.builder()
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
