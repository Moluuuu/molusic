package com.molu.audiofile.controller;


import com.alibaba.fastjson.JSONObject;
import com.molu.audiofile.service.AudioInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/file")
public class AudioInfoController {

    @Autowired
    AudioInfoService audioInfoService;

    @GetMapping("/tempdir") // 得到临时目录中的文件
    public JSONObject getTempDir() {
        Map<String, List<String>> tempInfo = audioInfoService.getTempInfo();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("audioInfo", tempInfo);
        return jsonObject;
    }

    @PostMapping(value = "/cleanup") // 清理临时目录
    public JSONObject cleanup(@RequestBody JSONObject jsonObject) {
        return audioInfoService.clearTempDir(jsonObject);
    }
}
