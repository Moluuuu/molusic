package com.molu.processing.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.MapUtil;
import com.molu.processing.dictionary.MFD;
import com.molu.processing.pojo.MlcMusic;
import com.molu.processing.service.MlcMusicService;
import com.molu.processing.service.UploadService;
import com.molu.processing.utils.FileUtils;
import com.molu.processing.utils.MusicUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/processing")
public class MlcMusicProcessingController {

    @Autowired
    UploadService uploadService;

    @Autowired
    MlcMusicService mlcMUsicService;


    // 上传操作
    @PostMapping(value = "/upload")
    public JSONObject getSongInfo(@RequestParam(value = "file") MultipartFile file
            , @RequestParam("type") String type, @RequestParam("translate") String translate) {
        boolean isTranslate = Boolean.parseBoolean(translate);
        MlcMusic music = new MlcMusic();
        music.setType(type);
        // 对上传文件进行处理，如 格式的处理(转 MP3) 歌词文件的获取和处理（保存并上传到远程）
        return mlcMUsicService.uploadProcessing(file, music, isTranslate);

    }

    // 上传完成后点击确认时执行的操作，作用是清空临时目录中的文件
    // TODO 这个接口迁到 file模块下，业务逻辑要改 不能删 .gitkeep占位文件
    @PostMapping(value = "/cleanup")
    public JSONObject getSongInfo(@RequestBody JSONObject jsonObject) {
        JSONArray deleteFiles = jsonObject.getJSONArray("deleteFiles");
        Map<String, String> map = new HashMap<>();
        if (CollectionUtils.isEmpty(deleteFiles)) {
            Map<String, String> delMusic = FileUtils.dirClean(new File(MFD.FILEPATH), "music");
            Map<String, String> delLyric = FileUtils.dirClean(new File(MFD.LYRICPATH), "lyric");
            Map<String, String> delUpload = FileUtils.dirClean(new File(MFD.UPLOADFILEPATH), "upload");
            if (MapUtil.isNotEmpty(delMusic)) {
                delMusic.forEach((k, v) -> {
                    MapUtil.putIfValNoNull(map, k, v);
                });
            }
            if (MapUtil.isNotEmpty(delLyric)) {
                delLyric.forEach((k, v) -> {
                    MapUtil.putIfValNoNull(map, k, v);
                });
            }
            if (MapUtil.isNotEmpty(delUpload)) {
                delUpload.forEach((k, v) -> {
                    MapUtil.putIfValNoNull(map, k, v);
                });
            }
        } else {
            List<String> delList = deleteFiles.toJavaList(String.class);
            AtomicInteger index = new AtomicInteger(0);
            delList.forEach(deleteFile -> {

                File file = new File(deleteFile);
                map.put(index.getAndIncrement() + "." + file.getName(), MusicUtils.deleteFile(file) ?
                        "删除成功" : "删除失败文件不存在，或出现异常!!!请检查服务器中对应资源");

            });
        }
        jsonObject.remove("deleteFile");
        if (map.isEmpty()) jsonObject.put("delInfo", "临时目录为空");
        else jsonObject.put("delInfo", map);

        return jsonObject;
    }

}
