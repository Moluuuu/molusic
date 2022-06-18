package com.molu.processing.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MlcMusicList {

    // 歌单标识 主键
    private int id;
    // 歌单名称
    private String name;
    // 歌单描述
    private String description;
    // 歌单封面
    private String cover;
    // 数据插入时间
    private Date createTime;
}
