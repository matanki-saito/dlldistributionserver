package com.popush.triela.common.DB;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileDao {
    private String md5;
    private byte[] data;
    private int assetId;
}
