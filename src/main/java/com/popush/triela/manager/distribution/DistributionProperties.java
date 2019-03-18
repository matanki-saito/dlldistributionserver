package com.popush.triela.manager.distribution;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "distribution")
public class DistributionProperties {
    private String s3BucketRegion;
    private String s3BucketName;
    private String cloudFrontDomainName;
}
