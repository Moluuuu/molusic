package com.molu.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MlcAlbum {

    // 专辑标识 主键
    private int id;
    // 专辑名称
    private String name;
    // 专辑发行时间 格式为 yyyy-MM-dd即可
    private Date releaseTime;
    // 流派
    private String type;
    // 歌手 / 乐队 / 团体名称
    private String singerName;
    // 曲目
    private int musicNum;
    // 专辑介绍
    private String introduce;
    // 专辑封面
    private String cover;
    // 数据插入时间
    private Date createTime;
}
