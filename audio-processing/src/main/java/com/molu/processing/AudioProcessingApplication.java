package com.molu.processing;

import com.molu.feign.client.AudioFileClient;
import com.molu.feign.config.DefaultConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(clients = {AudioFileClient.class}, defaultConfiguration = {DefaultConfig.class})
public class AudioProcessingApplication {

    public static void main(String[] args) {
        SpringApplication.run(AudioProcessingApplication.class, args);
    }

}
