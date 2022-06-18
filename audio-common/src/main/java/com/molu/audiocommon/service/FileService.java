package com.molu.audiocommon.service;


import com.alibaba.fastjson.JSONObject;
import com.molu.feign.client.AudioFileClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileService {

    @Autowired
    AudioFileClient client;

    public JSONObject getTempDirInfo() {
        return client.getTempDir();
    }
}
