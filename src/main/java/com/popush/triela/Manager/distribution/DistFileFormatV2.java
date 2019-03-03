package com.popush.triela.Manager.distribution;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class DistFileFormatV2 {
    @JsonProperty("file_md5")
    private String fileMd5;

    private String url;

    @JsonProperty("file_size")
    private long fileSize;
}
