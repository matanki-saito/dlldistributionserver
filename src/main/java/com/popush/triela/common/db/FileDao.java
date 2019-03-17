package com.popush.triela.common.db;

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
    private String dataUrl;
    private int assetId;
    private long dataSize; // byte
}
