package com.molu.audiofile.controller;


import com.alibaba.fastjson.JSONObject;
import com.molu.audiofile.service.AudioInfoService;
import com.molu.entity.MlcMusic;
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

    @PostMapping("/save") // 得到临时目录中的文件
    public boolean saveFileInfo(@RequestBody JSONObject jsonObject) {
        MlcMusic music = jsonObject.toJavaObject(MlcMusic.class);
        return audioInfoService.saveFileInfo(music);
    }
}
