package com.popush.triela.common.db;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExeEntity {
  private Integer id;
  private Integer gitHubRepoId;
  @NotNull
  private String md5;
  @NotNull
  private String version;
  private String description;
  private Integer distributionAssetId;
  private String phase;
  private Boolean autoUpdate;
}
