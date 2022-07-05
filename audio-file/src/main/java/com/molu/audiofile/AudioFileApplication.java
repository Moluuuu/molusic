package com.molu.audiofile;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.molu.audiofile.mapper")
@SpringBootApplication
public class AudioFileApplication {

    public static void main(String[] args) {
        SpringApplication.run(AudioFileApplication.class, args);
    }

}
