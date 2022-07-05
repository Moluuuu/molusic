package com.molu.feign.client;

import com.alibaba.fastjson.JSONObject;
import com.molu.entity.MlcMusic;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("audio-file")
public interface AudioFileClient {
    @GetMapping("/file/tempdir")
    JSONObject getTempDir();

    @PostMapping("/file/save")
    boolean saveFileInfo(@RequestBody JSONObject jsonObject);
}
