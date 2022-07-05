package com.molu.processing.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.shaded.org.checkerframework.checker.units.qual.A;
import com.molu.dictionary.MFD;
import com.molu.entity.MlcMusic;
import com.molu.feign.client.AudioFileClient;
import com.molu.processing.utils.AudioUtils;
import com.molu.processing.utils.MusicUtils;
import com.molu.processing.utils.ncmdump.Dump;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Objects;


@Service
public class AudioProcessingServiceImpl implements AudioProcessingService {

    @Autowired
    AudioFileClient fileClient;

    @Autowired
    UploadService uploadService;


    // 文件上传处理
    @SneakyThrows
    @Override
    public JSONObject processing(MultipartFile sourceFile, MlcMusic music, boolean isTranslate) {
        // 作用域提升
        String processedName = null;
        File target = null;
        boolean convert = true;
        JSONObject dataJson = new JSONObject();

        // 得到上传文件 原始文件名
        String sourceFileName = sourceFile.getOriginalFilename();
        // 去掉源文件名中的多余空格，防止一些意外情况发生
        String filename = Objects.requireNonNull(sourceFileName).replace(" - ", "-");
        // 得到文件名后缀为 .mp3 同名文件
        processedName = filename.replace(filename.substring(filename.lastIndexOf(".")), MFD.MP3);
        // 构造文件对象
        File source = new File(MFD.FILEPATH, filename);
        // 把上传的文件写入到本地
        sourceFile.transferTo(source);
        // 判断是否为 .mp3格式，如果是 .mp3格式转换标志位为 false
        if (filename.endsWith(MFD.MP3)) {
            convert = false;
        }
        // 如果上传的ncm
        if (filename.endsWith(MFD.NCM)) {
            Dump dump = new Dump(source);
            boolean execute = dump.execute();
            // 如果转换成功 构建新的 source对象 即转换得到的 xxxx.flac
            if (execute)
                source = new File(MFD.FILEPATH, filename.replace(filename.substring(filename.lastIndexOf(".")), MFD.FLAC));
        }

        // 构建一个 mp3文件对象
        target = new File(MFD.FILEPATH, processedName);
        // 如果上传文件不为 .mp3格式，则需要对文件进行转换，转换后再做处理
        if (convert) {
            // 转换操作 返回数据为转换信息  有源文件大小 转换后的大小 以及转换耗时
            dataJson.put("conversionInfo", AudioUtils.formatConversion(source, target));
        }

        MlcMusic musicInfo = MusicUtils.getSongInfo(target, music);
        // 得到音乐的信息
        dataJson.put("musicInfo", musicInfo);
        // 获取歌词文件并写入到上传路径
        MusicUtils.findLyrics(MusicUtils.getSongName(processedName), isTranslate);
        // 把歌词文件内容写入到 mp3文件里 并将文件保存到上传路径
        MusicUtils.writeAndSaveLyricFile(target);

        /*远程调用，将音频文件信息保存到数据库*/
        boolean saved = fileClient.saveFileInfo(dataJson.getJSONObject("musicInfo"));
        dataJson.put("saveState", "未存入数据库");
        if (saved){
            dataJson.put("qiniuInfo", uploadService.uploadToQiNiu(target));
            dataJson.put("saveState", "已存入数据库");
        }

        return dataJson;
    }
}
