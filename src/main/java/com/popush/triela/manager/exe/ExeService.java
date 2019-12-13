package com.popush.triela.manager.exe;

import com.popush.triela.common.db.ExeDto;
import com.popush.triela.common.db.ExeSelectCondition;
import com.popush.triela.common.github.GitHubApiService;
import com.popush.triela.db.ExeDao;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ExeService {
  final ExeDao exeDaoMapper;
  final GitHubApiService apiService;

  @Transactional
  public void save(@NonNull ExeForm form, int gitHubRepoId) {
    exeDaoMapper.insert(ExeDto.builder()
        .gitHubRepoId(gitHubRepoId)
        .md5(form.getMd5())
        .autoUpdate(form.isAutoUpdate())
        .version(form.getVersion())
        .description(form.getDescription())
        .phase(form.getPhase().isBlank() ? "prod" : form.getPhase())
        .build()
    );
  }

  @Transactional
  public void changeAutoUpdate(int id, boolean autoUpdate) {
    var data = ExeDto.builder().autoUpdate(autoUpdate).build();

    exeDaoMapper.update(
        ExeSelectCondition.builder().id(id).build(),
        data
    );
  }

  @Transactional
  public void delete(int id) {
    exeDaoMapper.delete(id);
  }

  @Transactional(readOnly = true)
  public List<ExeForm> list(ExeSelectCondition exeSelectCondition) {
    return exeDaoMapper
        .list(exeSelectCondition)
        .stream()
        .map(elem -> ExeForm
            .builder()
            .id(elem.getId())
            .autoUpdate(elem.getAutoUpdate())
            .description(elem.getDescription())
            .md5(elem.getMd5())
            .version(elem.getVersion())
            .distributionAssetId(elem.getDistributionAssetId())
            .phase(elem.getPhase())
            .build())
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public Page<ExeForm> page(ExeSearchConditionForm condition, Pageable pageable) {

    var list = list(ExeSelectCondition.builder().gitHubRepoId(condition.getGitHubRepoId()).build());

    int beginIndex = (int) pageable.getOffset();
    int endIndex = Math.min(beginIndex + pageable.getPageSize(), list.size());

    return new PageImpl<>(
        list.subList(beginIndex, endIndex),
        pageable,
        list.size()
    );
  }
}
