package com.molu.audiofile.controller;


import com.alibaba.fastjson.JSONObject;
import com.molu.audiofile.service.AudioInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/file")
public class AudioInfoController {

    @Autowired
    AudioInfoService audioInfoService;

    @GetMapping("/tempdir")
    public JSONObject getTempDir() {
        Map<String, List<String>> tempInfo = audioInfoService.getTempInfo();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("audioInfo", tempInfo);
        return jsonObject;
    }
}
