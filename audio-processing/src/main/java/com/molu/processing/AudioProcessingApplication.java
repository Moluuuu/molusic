package com.molu.processing;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.molu.processing.mapper")
@SpringBootApplication
public class AudioProcessingApplication {

    public static void main(String[] args) {
        SpringApplication.run(AudioProcessingApplication.class, args);
    }

}
