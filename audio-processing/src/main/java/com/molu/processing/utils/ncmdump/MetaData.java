package com.molu.processing.utils.ncmdump;

import com.alibaba.fastjson.JSON;

import java.nio.charset.StandardCharsets;

public class MetaData {
    public String musicName;
    public String[][] artist;
    public String album;
    public String format;

    public static MetaData read_from_json(byte[] json) {
        // byte[] => json字符串 => json字符串转 JSON对象 => MetaData对象
        return JSON.toJavaObject(JSON.parseObject(new String(json, StandardCharsets.UTF_8)), MetaData.class);
    }
}