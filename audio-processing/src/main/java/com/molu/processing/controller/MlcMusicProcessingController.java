package com.molu.processing.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.MapUtil;
import com.molu.dictionary.MFD;
import com.molu.entity.MlcMusic;
import com.molu.processing.service.AudioProcessingService;
import com.molu.processing.service.UploadService;
import com.molu.utils.FileUtils;
import com.molu.utils.MusicUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/processing")
public class MlcMusicProcessingController {

    @Autowired
    UploadService uploadService;

    @Autowired
    AudioProcessingService mlcMusicService;


    /**
     *
     * @param file 上传文件
     * @param jsonObject 其他上传选项信息，如歌曲分类，风格(type)、添加到的歌单(musicList[])、所属专辑(album)
     *                   歌词是否需要翻译(translate)、上传到的位置(uploadLocation)....
     * @return 返回上传状态信息
     */



    // 上传操作
    @PostMapping(value = "/upload")
    public JSONObject getSongInfo(
            @RequestParam(value = "file") MultipartFile file,
            @RequestHeader("uploadOptions") JSONObject jsonObject) {

        // 对上传文件进行处理，如 格式的处理(转 MP3) 歌词文件的获取和处理（保存并上传到远程）
        return mlcMusicService.processing(file,jsonObject);

    }



    // 上传完成后点击确认时执行的操作，作用是清空临时目录中的文件
    // TODO 这个接口迁到 file模块下，业务逻辑要改 不能删 .gitkeep占位文件

}
