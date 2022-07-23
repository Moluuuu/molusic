package com.molu.processing.config;

import com.molu.processing.pojo.Minio;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(Minio.class)
public class MinioConfig {

    @Autowired
    private Minio minio;

    @Bean
    public MinioClient minioClient(){
        return MinioClient.builder()
                .endpoint(minio.getEndPoint())
                .credentials(minio.getAccessKey(), minio.getSecretKey())
                .build();
    }
}