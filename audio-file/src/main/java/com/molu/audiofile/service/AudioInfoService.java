package com.molu.audiofile.service;

import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;

public interface AudioInfoService {
    //
    Map<String, List<String>> getTempInfo();

    JSONObject clearTempDir(JSONObject jsonObject);

}
