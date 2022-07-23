package com.molu.processing.service;

import com.alibaba.fastjson.JSONObject;
import com.molu.entity.MlcMusic;
import org.springframework.web.multipart.MultipartFile;

public interface AudioProcessingService {

//    String uploadFile(MultipartFile file);
//
//    String getPrivateFile(String fileKey);
//
//    boolean removeFile(String bucketName, String fileKey);

    public JSONObject processing(MultipartFile sourceFile, JSONObject jsonObject);
    public MlcMusic parseJSONObject(JSONObject jsonObject);
}
