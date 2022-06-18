package com.molu.feign.client;

import com.alibaba.fastjson.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("audio-file")
public interface AudioFileClient {
    @GetMapping("/file/tempdir")
    JSONObject getTempDir();
}
