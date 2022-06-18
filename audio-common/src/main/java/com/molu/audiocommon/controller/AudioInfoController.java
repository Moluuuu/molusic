package com.molu.audiocommon.controller;


import com.alibaba.fastjson.JSONObject;
import com.molu.audiocommon.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/common")
public class AudioInfoController {

    @Autowired
    FileService fileService;

    @GetMapping("/info/temp")
    public JSONObject getTempDir() {
        return fileService.getTempDirInfo();
    }
}
