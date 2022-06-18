package com.molu.processing.service;

import com.alibaba.fastjson.JSONObject;
import com.molu.processing.pojo.MlcMusic;
import org.springframework.web.multipart.MultipartFile;

public interface MlcMusicService {

//    String uploadFile(MultipartFile file);
//
//    String getPrivateFile(String fileKey);
//
//    boolean removeFile(String bucketName, String fileKey);

    public JSONObject uploadProcessing(MultipartFile sourceFile, MlcMusic music, boolean isTranslate);
}
