package com.molu.audiofile.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.molu.audiofile.service.AudioInfoService;
import com.molu.dictionary.MFD;
import com.molu.utils.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


@Service
public class AudioInfoServiceImpl implements AudioInfoService {
    @Override
    public Map<String, List<String>> getTempInfo() {
        File music = new File(MFD.FILEPATH);
        File lyric = new File(MFD.UPLOADFILEPATH);
        File upload = new File(MFD.LYRICPATH);
        return FileUtils.getDirFiles(music, lyric, upload);
    }


    @Override
    public JSONObject clearTempDir(JSONObject jsonObject) {
        // 如果未指定删除文件，执行删除全部
        if (jsonObject.isEmpty()) {
            jsonObject.put("delLyricInfo", FileUtils.dirClean(new File(MFD.LYRICPATH), "lyric"));
            jsonObject.put("delUploadInfo", FileUtils.dirClean(new File(MFD.UPLOADFILEPATH), "upload"));
            jsonObject.put("delMusicInfo", FileUtils.dirClean(new File(MFD.FILEPATH), "music"));
            return jsonObject;
        }
        // 获取 json对象每一对 k v
        Map<String, String> map = new HashMap<>();
        AtomicInteger index = new AtomicInteger(1);

        if (jsonObject.containsKey("deleteMusics")) {
            // 得到对应数组
            JSONArray deleteMusics = jsonObject.getJSONArray("deleteMusics");
            deleteMusics.forEach(music -> {
                map.put("music_" + index + " : " + music, (FileUtils.deleteFile(MFD.FILEPATH + music)) ? "删除成功" : "删除失败，请检查目录或文件是否存在");
                index.getAndIncrement();
            });
            jsonObject.remove("deleteMusics");
        }

        if (jsonObject.containsKey("deleteLyrics")) {
            // 得到对应数组
            JSONArray deleteLyrics = jsonObject.getJSONArray("deleteLyrics");

            deleteLyrics.forEach(lyric -> {
                map.put("lyric_" + index + " : " + lyric, (FileUtils.deleteFile(MFD.LYRICPATH + lyric)) ? "删除成功" : "删除失败，请检查目录或文件是否存在");
                index.getAndIncrement();
            });
            jsonObject.remove("deleteLyrics");
        }

        if (jsonObject.containsKey("deleteUploads")) {
            // 得到对应数组
            JSONArray deleteUploads = jsonObject.getJSONArray("deleteUploads");
            deleteUploads.forEach(uploadFile -> {
                map.put("upload_" + index + " : " + uploadFile, (FileUtils.deleteFile(MFD.UPLOADFILEPATH + uploadFile)) ? "删除成功" : "删除失败，请检查目录或文件是否存在");
                index.getAndIncrement();
            });
            jsonObject.remove("deleteUploads");
        }
        jsonObject.put("delInfo", map);
        return jsonObject;
    }

}
