package com.molu.feign.client;

import com.alibaba.fastjson.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("audio-file")
public interface AudioFileClient {
    @GetMapping("/file/tempdir")
    JSONObject getTempDir();

    @PostMapping("/file/cleanup")
    JSONObject cleanup(@RequestBody JSONObject jsonObject);


    @PostMapping("/data/save")
    boolean saveFileInfo(@RequestBody JSONObject jsonObject);
}
