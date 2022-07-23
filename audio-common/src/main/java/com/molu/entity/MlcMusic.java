package com.molu.entity;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.nacos.shaded.com.google.gson.JsonArray;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
//@AllArgsConstructor
public class MlcMusic {
    // 音乐文件标识
    private String id;
    // 音乐名称
    private String name;
    // 音乐文件名称
    private String fileName;
    // 音乐文件大小（不精确）
    private String fileSize;
    // 专辑名称
    private String albumName;
    // 专辑艺术家
    private String albumArtist;
    // 歌手 / 团体 / 乐队
    private String singerName;
    // 曲类 / 流派
    private String type;
    // 歌曲 / 专辑封面
    private String albumCover;
    // 所在歌单
    private String[] musicList;
    // 数据插入时间
    private Date createTime;
    // 时长
    private String duration;
    // 发行年份
    private String year;
    // 专辑排序
    private String Track;


    public MlcMusic() {
        this.id = UUID.randomUUID().toString().replace("-", "");
        this.createTime = new Date();
        this.musicList = ArrayUtils.add(new String[]{},"default");
    }

}
