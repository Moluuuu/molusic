package com.molu.feign.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;


public class DefaultConfig {

    @Bean
    Logger.Level logLevel() {
        return Logger.Level.FULL;
    }

}
