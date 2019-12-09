package com.popush.triela.manager.exe;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ExeForm implements Serializable, Comparable<ExeForm> {
  /**
   * ID
   */
  private int id;

  /**
   * eu4.exe or ck2game.exe message digest
   */
  @NotNull
  @Size(min = 32, max = 32)
  private String md5;

  /**
   * ex) 1.28.2.1-beta20
   * ex) 3.0.0.0
   */
  @NotNull
  @Pattern(regexp = "[0-3]+\\.[0-9]+\\.[0-9]+\\.[0-9]+(-(beta|hotfix)[0-9]+)?")
  private String version;

  /**
   * ex) open-beta only
   */
  @Size(max = 512)
  private String description;

  /**
   *
   */
  private Integer distributionAssetId;

  /**
   * ex) prod(default), dev, alpha, beta etc.
   */
  @Size(max = 32)
  @Pattern(regexp = "[a-zA-Z0-9_]*")
  private String phase;

  @Override
  public int compareTo(ExeForm o) {
    return this.version.compareTo(o.version);
  }
}
