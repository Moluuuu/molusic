package com.molu.processing.pojo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class Minio {
    private String endPoint;
    private String accessKey;
    private String secretKey;
}