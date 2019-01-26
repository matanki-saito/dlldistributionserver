package com.popush.triela.common.AWS;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3ClientConfig {
    @Bean
    S3Client s3ClientProvider() {
        Region region = Region.AP_NORTHEAST_1;
        return S3Client.builder().region(region).build();
    }
}
