package com.molu.processing.service;

import com.alibaba.fastjson.JSONObject;
import com.molu.processing.dictionary.MFD;
import com.molu.processing.mapper.MlcMusicMapper;
import com.molu.processing.pojo.MlcMusic;
import com.molu.processing.utils.AudioUtils;
import com.molu.processing.utils.DataUtils;
import com.molu.processing.utils.MusicUtils;
import com.molu.processing.utils.ncmdump.Dump;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Objects;


@Service
public class MlcMusicServiceImpl implements MlcMusicService {
    @Autowired
    MlcMusicMapper mlcMusicMapper;

    @Autowired
    UploadService uploadService;


    // 文件上传处理
    @SneakyThrows
    @Override
    public JSONObject uploadProcessing(MultipartFile sourceFile, MlcMusic music, boolean isTranslate) {

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
        if (filename.endsWith(".ncm")) {
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


        // 得到音乐的信息
        dataJson.put("musicInfo", MusicUtils.getSongInfo(target, music));
        // 获取歌词文件并写入到上传路径
        MusicUtils.findLyrics(MusicUtils.getSongName(processedName), isTranslate);
        // 把歌词文件内容写入到 mp3文件里 并将文件保存到上传路径
        MusicUtils.writeAndSaveLyricFile(target);
        // 使用新路径
//            target = new File(MFD.UPLOADFILEPATH, URLEncoder.encode(target.getName(),"utf-8"));

        // 数据库操作，先查一下数据库中有没有该音乐文件的数据
        List<MlcMusic> mlcMusics = mlcMusicMapper.selectByMap(DataUtils.selectMap("file_name", processedName));
        // 没有就执行插入
        if (mlcMusics.isEmpty()) {
            mlcMusicMapper.insert(music);
            dataJson.put("qiniuInfo", uploadService.uploadToQiNiu(target));
            System.out.println("处理完成");
            return dataJson;
        }
        // 如果存在就进行比较，如果一样就跳过，否则 执行更新操作
        MlcMusic mlcMusic = mlcMusics.get(0);
        // 这里的操作是因为 id是随机产生的， creteTime是当前时间
        // 所以二者不可能相同 赋值后二者相同 如果其他数据也相同则不会通过判断
        music.setId(mlcMusic.getId());
        mlcMusic.setCreateTime(music.getCreateTime());
        if (!Objects.equals(mlcMusic, music)) {
            mlcMusicMapper.updateById(music);
        }
        System.out.println("处理完成");
        dataJson.put("qiniuInfo", uploadService.uploadToQiNiu(target));

        return dataJson;
    }


    /*public static void main(String[] args) {
        try {
            File source = new File("D:\\IDEAproject\\molusic\\audio-processing\\src\\main\\resources\\music\\野外合作社-阳光中的向日葵（Live）.flac");
            File target = new File("D:\\IDEAproject\\molusic\\audio-processing\\src\\main\\resources\\music\\野外合作社-阳光中的向日葵（Live）.mp3");

            //Audio Attributes
            AudioAttributes audio = new AudioAttributes();
            audio.setCodec("libmp3lame");
            audio.setBitRate(128000);
            audio.setChannels(2);
            audio.setSamplingRate(44100);

            //Encoding attributes
            EncodingAttributes attrs = new EncodingAttributes();
            attrs.setFormat("mp3");
            attrs.setAudioAttributes(audio);

            //Encode
            Encoder encoder = new Encoder();
            encoder.encode(new MultimediaObject(source), target, attrs);

        } catch (Exception ex) {
            System.out.println(ex);
        }

    }*/
}
