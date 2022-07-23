package com.molu.processing.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.molu.audiofile.utils.MultipartFile2File;
import com.molu.dictionary.MFD;
import com.molu.entity.MlcMusic;
import com.molu.feign.client.AudioFileClient;
import com.molu.processing.utils.AudioUtils;
import com.molu.processing.utils.minio.MinioUtils;
import com.molu.processing.utils.ncmdump.Dump;
import com.molu.utils.MusicUtils;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Objects;


@Service
public class AudioProcessingServiceImpl implements AudioProcessingService {

    @Autowired
    AudioFileClient fileClient;

    @Autowired
    UploadService uploadService;

    @Autowired
    MinioUtils minioUtils;


    // 文件上传处理 todo 文件上传和文件格式转换最好分开，记得改一下 controller
    @SneakyThrows
    @Override
    public JSONObject processing(MultipartFile sourceFile,JSONObject jsonObject) {
        // 作用域提升
        String processedName = null;
        File target = null;
        boolean convert = true;
        JSONObject dataJson = new JSONObject();
        MlcMusic music = parseJSONObject(jsonObject);
        Boolean isTranslate = jsonObject.getBoolean("isTranslate");
        Boolean uploadLocation = jsonObject.getBoolean("uploadLocation");

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
        // 获取歌词信息
        List<String> lyricInfo = MusicUtils.processingLyric(MusicUtils.getLyric(MusicUtils.getSongName(processedName), isTranslate));

        // 把歌词文件内容写入到 mp3文件里 并将文件保存到上传路径
        MusicUtils.genAndSaveLyricFile(lyricInfo,target);

        /*远程调用，将音频文件信息保存到数据库*/
        boolean saved = fileClient.saveFileInfo(dataJson.getJSONObject("musicInfo"));
        dataJson.put("saveState", "未存入数据库");
        if (saved){
            dataJson.put("saveState", "已存入数据库");
            if (uploadLocation){
                // todo 这里可以获取一下 minio的返回信息，add到dataJson，在工具类里面做
                minioUtils.upload(MultipartFile2File.parse(target),"musics");
                return dataJson;
            }
            dataJson.put("qiniuInfo", uploadService.uploadToQiNiu(target));
        }

        return dataJson;
    }

    @Override
    public MlcMusic parseJSONObject(JSONObject jsonObject) {
        MlcMusic music = new MlcMusic();
        // 获取类型，如果不为空设置进music对象
        String type = jsonObject.getString("type");
        if (StringUtils.isNotBlank(type)){
            music.setType(type);
        }
        JSONArray musicList = jsonObject.getJSONArray("musicList");
        // 如果不勾选 则存放至 default 歌单
        if (musicList.contains("default")){
            musicList.remove("default");
            String[] musicMusicList = music.getMusicList();
            if (musicList.size() > 0){
                List<String> list = musicList.toJavaList(String.class);
                // 把传入的 musicList元素添加到 music.musicList属性中
                music.setMusicList(ArrayUtils.addAll(musicMusicList, list.toArray(new String[0])));
            }
        }
        // 前端做校验，如果未指定专辑 则创建同名专辑，这边始终有值即可
        music.setAlbumName(jsonObject.getString("album"));


        return music;
    }
}
