package com.popush.triela.exemanager;

import com.popush.triela.common.DB.ExeDao;
import com.popush.triela.common.DB.ExeDaoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ExeRegisterService {
    final ExeDaoMapper exeDaoMapper;

    public void save(ExeRegisterForm form){
        exeDaoMapper.upsert(ExeDao.builder()
                .md5(form.getMd5())
                .product(form.getProduct())
                .version(form.getVersion())
                .description(form.getDescription())
                .build()
        );
    }

    public List<ExeRegisterForm> list(){
        return exeDaoMapper.list().stream().map(
                elem-> ExeRegisterForm
                        .builder()
                        .description(elem.getDescription())
                        .md5(elem.getMd5())
                        .product(elem.getProduct())
                        .version(elem.getVersion())
                        .distributionAssetId(elem.getDistributionAssetId())
                        .build()
        ).collect(Collectors.toList());
    }
}
