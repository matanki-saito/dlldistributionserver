package com.popush.triela.exeregister;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExeRegisterForm implements Serializable {
    /**
     * ck2 or eu4 or ...
     */
    @NotNull
    @Size(min = 1, max = 32)
    private String product;

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
    @Pattern(regexp = "[0-3]+\\.[0-9]+\\.[0-9]+\\.[0-9]+(-beta[0-9]+)?")
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
}
