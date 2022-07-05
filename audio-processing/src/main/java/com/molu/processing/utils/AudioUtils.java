package com.molu.processing.utils;

import com.molu.dictionary.MFD;
import com.molu.processing.utils.ncmdump.Dump;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;
import ws.schild.jave.*;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class AudioUtils {
    private static final Logger logger = LoggerFactory.getLogger(AudioUtils.class);

    private static final int BITRATE = 3200000;
    private static final int Channels = 2;
    private static final int SamplingRate = 44100;
    static AudioAttributes audio = new AudioAttributes();


    public static void mp3toWav(File source, File target) {
        audio.setCodec(MFD.CODEC);
        audio.setBitRate(BITRATE);
        audio.setChannels(Channels);
        audio.setSamplingRate(SamplingRate);
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat("wav");
        attrs.setAudioAttributes(audio);
        Encoder encoder = new Encoder();
        MultimediaObject sourceObj = new MultimediaObject(source);
        try {
            encoder.encode(sourceObj, target, attrs);
            logger.info("转换成功");
        } catch (EncoderException e) {
            e.printStackTrace();
            logger.warn("WAV 转换至 MP3时出现异常，检查上传的文件");
        }
    }

    @SneakyThrows
    public static void ncm2flac(File... files) {
        if (files.length > 0) {
            for (File file : files) {
                Dump dump = new Dump(file);
                dump.execute();
            }
        } else {
            logger.warn("NCM 转换至 FLAC时出现异常，检查上传的文件");
        }
    }

    public static void flacToMp3(File source, File target) {
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec(MFD.CODEC);
        audio.setBitRate(BITRATE);
        audio.setChannels(Channels);
        audio.setSamplingRate(SamplingRate);
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat("mp3");
        attrs.setAudioAttributes(audio);
        Encoder encoder = new Encoder();
        MultimediaObject sourceObj = new MultimediaObject(source);
        try {
            encoder.encode(sourceObj, target, attrs);
            logger.info("转换成功");
        } catch (EncoderException e) {
            e.printStackTrace();
            logger.warn("FLAC 转换至 MP3时出现异常，检查上传的文件");
        }
    }


    public static Map<String, String> formatConversion(File source, File target) {
        double size = source.length();
        Map<String, String> map = new HashMap<>();
        audio.setCodec(MFD.CODEC);
        audio.setBitRate(BITRATE);
        audio.setChannels(Channels);

        audio.setSamplingRate(SamplingRate);
        EncodingAttributes attrs = new EncodingAttributes();
        if (target.getName().endsWith(MFD.MP3)) attrs.setFormat("mp3");
        attrs.setAudioAttributes(audio);
        Encoder encoder = new Encoder();
        MultimediaObject sourceObj = new MultimediaObject(source);
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            encoder.encode(sourceObj, target, attrs);
            stopWatch.stop();
            double conversionSize = target.length();
            map.put("originFileSize", BigDecimal.valueOf(size / (1024 * 1024)).setScale(2, RoundingMode.HALF_UP) + "MB");
            map.put("compressedSize", BigDecimal.valueOf(conversionSize / (1024 * 1024)).setScale(2, RoundingMode.HALF_UP) + "MB");
            map.put("timeConsumed", BigDecimal.valueOf(stopWatch.getTotalTimeSeconds()).setScale(2, RoundingMode.HALF_UP) + "s");
        } catch (EncoderException e) {
            e.printStackTrace();
            logger.warn("转换至 MP3时出现异常，检查上传的文件");
        }
        return map;
    }


}

