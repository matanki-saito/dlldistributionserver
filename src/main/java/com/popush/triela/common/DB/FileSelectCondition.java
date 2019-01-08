package com.popush.triela.common.DB;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FileSelectCondition {
    private String md5;
    private int assetId;
    private String distributedExeMd5;
    private int gitHubRepoId;
}
