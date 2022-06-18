package com.molu.audiofile;

import com.alibaba.fastjson.JSONObject;
import com.molu.audiofile.service.AudioInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest
class AudioCommonApplicationTests {


    @Autowired
    AudioInfoService audioInfoService;

    @Test
    void contextLoads() {
        Map<String, List<String>> tempInfo = audioInfoService.getTempInfo();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", tempInfo);
        System.out.println(jsonObject);
    }

}
