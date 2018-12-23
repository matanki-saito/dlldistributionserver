package com.popush.triela.common.DB;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FileSelectCondition {
    private String md5;
    private String assetId;
    private String distributedExeMd5;
}
