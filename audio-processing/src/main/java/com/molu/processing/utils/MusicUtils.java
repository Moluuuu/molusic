package com.molu.processing.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.molu.dictionary.MFD;
import com.molu.entity.MlcMusic;
import com.mpatric.mp3agic.*;
import com.qiniu.util.StringUtils;
import lombok.SneakyThrows;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class MusicUtils {
    /**
     *
     * @param songName 歌名
     * @param translate 是否翻译
     * @return 返回得到的歌词信息
     */
    @SneakyThrows
    public static Map<String,List<String>> getLyric(String songName,boolean translate){
        Map<String, String> musicIds = getMusicIdByName(songName);
        StringBuilder finalLrc = new StringBuilder();
        String id = "";
        if (!musicIds.isEmpty()) {
            id = musicIds.get("0");
        }
        URL lyricURL = null;
        URL translateURL = null;
        Map<String,List<String>> lyricMap = new HashMap<>();

        // 纯音乐
        String pureMusicState = "{\"nolyric\":true,\"code\":200\"}";
        String pureMusicState1 = "{\"code\":200,\"nolyric\":true}";
        // 无歌词
        String emptyLyricState = "{\"code\":200}";
        // 无翻译歌词
        String emptyTranslateState = "{\"sgc\":true,\"sfy\":true,\"qfy\":true,\"needDesc\":true,\"code\":200,\"briefDesc\":null}";

        // 默认走的url，获取原歌词
        lyricURL = new URL("http://music.163.com/api/song/media?id=" + id);
        InputStream lyricStream = lyricURL.openStream();
        InputStreamReader lyricStreamReader = new InputStreamReader(lyricStream, StandardCharsets.UTF_8);
        BufferedReader lyricBufferedReader = new BufferedReader(lyricStreamReader);
        String lyricResult = null;
        JSONObject jsonObject = new JSONObject();
        String lyric = null;


        while ((lyricResult = lyricBufferedReader.readLine())!=null){
            jsonObject = JSON.parseObject(lyricResult); // 取得响应结果
        }

        String resultString = jsonObject.toJSONString();
        // 如果响应结果为空 或者与无歌词 纯音乐的返回结果一致；表示没有原歌词，故没有翻译的必要
        if (StringUtils.isNullOrEmpty(resultString) || resultString.equals(pureMusicState) || resultString.equals(emptyLyricState) || resultString.equals(pureMusicState1)){
            translate = false;
            lyric = "{\"lyric\":\"[00:10]暂未获取到歌词 \n[00:20]请确认当前音乐是否为纯音乐 \n[00:30]您可以导入自定义歌词 \"}";
            // 给出提示
        }
        // 正常情况
        if (jsonObject.containsKey("lyric")){
            lyric = String.valueOf(jsonObject.get("lyric"));
        }
        assert lyric != null;
        String[] split = lyric.split("\n");
        List<String> list = Arrays.asList(split);
        List<String> originalLyrics = new ArrayList<>();
        list.forEach(lineLyric->{
            lineLyric = lineLyric.trim();
            if (!lineLyric.endsWith("]") && !StringUtils.isNullOrEmpty(lineLyric)){
                originalLyrics.add(lineLyric);
            }
        });
        lyricMap.put("originalLyric",originalLyrics);

        lyricStream.close();
        lyricStreamReader.close();
        lyricBufferedReader.close();


        if (translate){
            translateURL = new URL("https://music.163.com/api/song/lyric?os=pc&id=" + id + "&tv=-1");
            // 如果需要翻译
            InputStream translateStream = translateURL.openStream();
            InputStreamReader translateStreamReader = new InputStreamReader(translateStream, StandardCharsets.UTF_8);
            BufferedReader translateBufferedReader = new BufferedReader(translateStreamReader);
            String translateResult = null;
            JSONObject translateJsonObject = new JSONObject();


            while ((translateResult = translateBufferedReader.readLine())!=null){
                translateJsonObject = JSON.parseObject(translateResult);
            }
            String translateString = translateJsonObject.toJSONString();

            // 如果翻译为空，跳过翻译
            if (translateString.equals(emptyTranslateState)){
                return lyricMap;
            }

            if (translateJsonObject.containsKey("tlyric")){
                JSONObject tlyric = translateJsonObject.getJSONObject("tlyric");
                String translateLyric = null;
                if (tlyric.containsKey("lyric")){
                    translateLyric = tlyric.getString("lyric");
                }
                // 因为有些歌词爬下来之后会使用回车 有的会使用 \n 如果只做一种校验会造成 spilt不生效的问题，但如果混用还是会出问题
                if (!StringUtils.isNullOrEmpty(translateLyric)){
                    String [] translateSplit = null;
                    if (translateLyric.split("\\n").length > translateLyric.split("\n").length){
                        translateSplit = translateLyric.split("\\n");
                    }else {
                        translateSplit = translateLyric.split("\n");
                    }
                    List<String> translateList = Arrays.asList(translateSplit);
                    List<String> translateLyrics = new ArrayList<>();
                    translateList.forEach(lineTranslateLyric->{
                        lineTranslateLyric = lineTranslateLyric.trim();
                        if (!lineTranslateLyric.endsWith("]") && !StringUtils.isNullOrEmpty(lineTranslateLyric)){
                            translateLyrics.add(lineTranslateLyric);
                        }
                    });
                    lyricMap.put("translateLyric",translateLyrics);
                }
            }
            translateStream.close();
            translateStreamReader.close();
            translateBufferedReader.close();
        }
        return lyricMap;
    }

    /**
     *
     * @param lyricMap 歌词信息
     * @return 处理过的歌词；如果存在翻译歌词且符合规范 会把原歌词和翻译歌词放到一列
     */
    public static List<String> processingLyric(Map<String,List<String>> lyricMap){
        // 传进来的map只有两种可能 带翻译歌词的和不带的
        List<String> originalLyric = lyricMap.get("originalLyric");
        List<String> processingLyrics = new ArrayList<>();
        if (lyricMap.containsKey("translateLyric")){
            // 如果有翻译歌词就需要进一步做处理了
            List<String> translateLyric = lyricMap.get("translateLyric");
            // 确保歌词长度一致
            if (translateLyric.size() == originalLyric.size()){
                for (int i = 0; i < translateLyric.size(); i++) {
                    String lineTranslateLyric = translateLyric.get(i);
                    if (originalLyric.get(i).substring(0,10).equals(lineTranslateLyric.substring(0,10))){
                        String processingLyric = lineTranslateLyric.substring(10);
                        processingLyrics.add(originalLyric.get(i) + " " + processingLyric);
                    }
                }
            }
        }
        // 如果不带 不做处理 直接把原歌词返回回去，或者原歌词长度与翻译歌词长度不匹配时 不做处理
        return processingLyrics;
    }

    // 通过歌名获取 id，返回一个 id集合，因为可能存在同名歌曲的情况
    public static Map<String, String> getMusicIdByName(String name) {
        URL url = null;
        JSONObject jsonObject = new JSONObject();
        Map<String,String> map = new HashMap<>();
        try {
            url = new URL("https://music.163.com/api/search/pc?s=" + URLEncoder.encode(name, "utf-8") + "&type=1");
            URLConnection urlConnection = url.openConnection();
            urlConnection.setRequestProperty("Cookie", "NMTID=00OcLvppKp14Gq3mE10tYce4J_2L_UAAAGARyM31A");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8));
            String jsonString = null;
            while ((jsonString = bufferedReader.readLine())!=null){
                String s = bufferedReader.readLine();
                jsonObject = JSON.parseObject(jsonString);
            }
            JSONObject result = jsonObject.getJSONObject("result");
            JSONArray songs = result.getJSONArray("songs");
            for (int i = 0; i < songs.size(); i++) {
                JSONObject song = (JSONObject) songs.get(i);
                map.put(i + "",song.getString("id"));
            }
            bufferedReader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    // 获取歌曲信息
    public static MlcMusic getSongInfo(File song, MlcMusic music) {
        try {
            float size = song.length() / (1024f * 1024f);
            BigDecimal scale = new BigDecimal(size).setScale(2, RoundingMode.CEILING);
            music.setFileSize(scale + "MB");
            Mp3File file = new Mp3File(song);
            music.setDuration(file.getLengthInSeconds() / 60 + ":" + file.getLengthInSeconds() * 2 % 60); // 音乐时长
            ID3v2 id3v2Tag = file.getId3v2Tag();
            music.setSingerName(id3v2Tag.getArtist()); // 歌手
            music.setAlbumArtist(id3v2Tag.getAlbumArtist()); // 专辑艺术家
            music.setAlbumName(id3v2Tag.getAlbum()); // 专辑
            music.setYear(id3v2Tag.getYear()); // 发行年份
            music.setTrack((id3v2Tag.getTrack())); // 专辑顺序
            music.setName(id3v2Tag.getTitle()); // 歌名
            music.setFileName(song.getName());
        } catch (InvalidDataException | UnsupportedTagException | IOException e) {
            e.printStackTrace();
        }
        return music;
    }
    // 删除文件
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        System.out.println("要删除的文件所在位置: " + filePath);
        if (file.exists()) return file.delete();
        else {
            System.out.println("Failed To Delete");
            return false;
        }
    }

    // 删除文件，重载
    public static boolean deleteFile(File file) {
        System.out.println("要删除的文件所在位置: " + file.getAbsolutePath());
        if (file.exists()) return file.delete();
        else {
            System.out.println("Failed To Delete");
            return false;
        }
    }

    // 获取歌名
    public static String getSongName(String fileName) {
        if (!StringUtils.isNullOrEmpty(fileName)) return fileName.substring(0, fileName.lastIndexOf("."));
        return "传入的文件名为空";
    }

    // 生成歌词文件并将歌词信息写入到 目标音频文件
    @SneakyThrows
    public static void genAndSaveLyricFile(List<String> list, File target) {
        // 得到 mp3对象
        Mp3File mp3File = new Mp3File(target);
        // 得到歌名
        String songName = getSongName(target.getName());
        if (!CollectionUtils.isEmpty(list)){
            // 如果歌词不为空 把歌词写入到mp3对象中
            ID3v2 id3v2Tag = mp3File.getId3v2Tag();
            id3v2Tag.setLyrics(list.toString());
            // 将处理后的 mp3文件保存到 上传路径
            mp3File.save(MFD.UPLOADFILEPATH + songName);

            File lyricFile = new File(MFD.LYRICPATH,songName+MFD.LRC);
            if (!lyricFile.exists()){
                lyricFile.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(lyricFile);
            BufferedWriter writer = new BufferedWriter(fileWriter);

            list.forEach(lineLyric->{
                try {
                    writer.write(lineLyric);
                    writer.newLine();
                } catch (IOException e) {
                    throw new RuntimeException("歌词文件写入异常");
                }
            });
            fileWriter.flush();
            writer.close();
        }
    }
}
