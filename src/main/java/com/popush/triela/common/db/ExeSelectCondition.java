package com.popush.triela.common.db;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ExeSelectCondition {
    private Integer id;
    private int gitHubRepoId;
    private String md5;
    private String phase;
}
