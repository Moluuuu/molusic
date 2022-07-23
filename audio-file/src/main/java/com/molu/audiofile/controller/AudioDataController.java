package com.molu.audiofile.controller;


import com.alibaba.fastjson.JSONObject;
import com.molu.audiofile.service.AudioDataService;
import com.molu.entity.MlcMusic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("data")
@RestController
public class AudioDataController {

    @Autowired
    AudioDataService audioDataService;

    @PostMapping("/save") // 保存到数据库
    public boolean saveFileInfo(@RequestBody JSONObject jsonObject) {
        MlcMusic music = jsonObject.toJavaObject(MlcMusic.class);
        return audioDataService.saveFile(music);
    }

}
