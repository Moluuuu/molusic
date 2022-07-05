package com.molu.processing.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.molu.dictionary.MFD;
import com.molu.entity.MlcMusic;
import com.mpatric.mp3agic.*;
import com.qiniu.util.StringUtils;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ArrayUtils;
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

    private static LinkedList<String> originalLrcList = new LinkedList<>();
    private static LinkedList<String> translationLrcList = new LinkedList<>();

    public static void findLyrics(String songName, boolean translate) {
        Map<String, String> musicIds = getMusicIdByName(songName);
        StringBuilder finalLrc = new StringBuilder();
        String songId = "";
        if (!musicIds.isEmpty()) {
            songId = musicIds.get("0");
        }

        // 获取并处理原歌词
        lrcProcessor(getOriginalLrc(songId, false), originalLrcList);
        // 获取并处理翻译歌词
        if (translate) lrcProcessor(getOriginalLrc(songId, true), translationLrcList);
        // 组合原歌词与翻译歌词
        if (translationLrcList.size() > 0 && translate) {
            for (int i = 0; i < originalLrcList.size(); i++) {
                boolean isFindTransLrc = false;
                for (int j = 0; j < translationLrcList.size(); j++) {

                    // 对比时间戳
                    String originalLrcTimestamp = originalLrcList.get(i).substring(0, originalLrcList.get(i).indexOf("]") + 1);
                    String translationLrcTimestamp = translationLrcList.get(j).substring(0, translationLrcList.get(j).indexOf("]") + 1);

                    if (originalLrcTimestamp.equals(translationLrcTimestamp)) {
                        // 得到每一行单独的原歌词和翻译歌词
                        String lrc = originalLrcList.get(i);
                        String tlrc = translationLrcList.get(j);
                        // 如果原歌词最后不是以空格结尾 加上空格
                        if (!(lrc.endsWith(" "))) lrc += " ";
                        // 如果翻译歌词开头以空格开头，截去空格
                        if (lrc.startsWith(" ")) tlrc = tlrc.substring(1);
                        finalLrc.append(lrc).append(tlrc.replace(translationLrcTimestamp, "")).append("\n");
                        isFindTransLrc = true;
                    }
                }

                if (!isFindTransLrc) {
                    finalLrc.append(originalLrcList.get(i)).append("\n");
                }
            }
        } else {
            for (int i = 0; i < originalLrcList.size(); i++) {
                finalLrc.append(originalLrcList.get(i)).append("\r\n");
            }
        }

        // 最终输出
        try {
            System.out.println("正在保存文件...");
            FileOutputStream fs = new FileOutputStream(MFD.UPLOADFILEPATH + File.separator + songName + MFD.LRC);
            PrintStream p = new PrintStream(fs);
            p.println(finalLrc);
            p.close();
            fs.close();
            originalLrcList.clear();
            translationLrcList.clear();

        } catch (FileNotFoundException e) {
            System.out.println("输出文件失败: " + e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public static Map<String,List<String>> getLyric(String id,boolean translate){
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
                    }
                    translateSplit = translateLyric.split("\n");
                    List<String> translateList = Arrays.asList(translateSplit);
                    List<String> translateLyrics = new ArrayList<>();
                    translateList.forEach(lineTranslateLyric->{
                        lineTranslateLyric = lineTranslateLyric.trim();
                        if (!lineTranslateLyric.endsWith("]") &&!StringUtils.isNullOrEmpty(lineTranslateLyric)){
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

    // 获取原歌词
    private static String getOriginalLrc(String songId, boolean isTranslationLrc) {
        if (isTranslationLrc) {
            System.out.println("正在获取翻译歌词...");
        } else {
            System.out.println("正在获取歌词...");
        }

        String temp = null;

        try {
            URL localURL = null;

            if (isTranslationLrc) {
                localURL = new URL("https://music.163.com/api/song/lyric?os=pc&id=" + songId + "&tv=-1");
            } else {
                localURL = new URL("http://music.163.com/api/song/media?id=" + songId);
            }

            InputStream localInputStream = localURL.openStream();
            InputStreamReader localInputStreamReader = new InputStreamReader(localInputStream, StandardCharsets.UTF_8);
            BufferedReader localBufferedReader = new BufferedReader(localInputStreamReader);

            String str = null;
            while ((str = localBufferedReader.readLine()) != null) {
                temp = str;
            }
            if (Objects.equals(temp, "{\"uncollected\":true,\"sgc\":true,\"sfy\":true,\"qfy\":true,\"needDesc\":true,\"code\":200,\"briefDesc\":null}"))
                temp = "{\"tlyric\":{\"lyric\":\"[00:10]Lyrics not yet available\n[00:20]Please confirm whether the current music is pure music\n[00:30]You can import custom lyrics\"}}";
            if (Objects.equals(temp, "{\"code\":200}"))
                temp = "{\"lyric\":\"[00:10]暂未获取到歌词 \n[00:20]请确认当前音乐是否为纯音乐 \n[00:30]您可以导入自定义歌词 \"}";
            localBufferedReader.close();
            localInputStreamReader.close();
            localInputStream.close();
        } catch (Exception localException) {
            if (isTranslationLrc) {
                System.out.println("获取翻译歌词失败: " + localException);
            } else {
                System.out.println("获取歌词失败: " + localException);
            }
        }

        return temp;
    }

    // 处理歌词
    private static void lrcProcessor(String lrc, LinkedList<String> mLrcList) {
        String[] mLrc = null;
        // 处理原歌词非歌词数据
        if (mLrcList == originalLrcList) {
            JSONObject jsonObject = JSON.parseObject(lrc);
            String lyric = jsonObject.getString("lyric");
            if (lyric != null) {
                System.out.println("获取歌词成功");
                mLrc = lyric.split("\n");
            } else {
                System.out.println("无法获取歌词");
            }
        }
        // 处理翻译歌词非歌词数据
        else {
            JSONObject jsonObject = JSON.parseObject(lrc);
            JSONObject tlyric = jsonObject.getJSONObject("tlyric");
            String lyric = tlyric.getString("lyric");
            if (lyric != null) {
                System.out.println("获取翻译歌词成功");
                mLrc = lyric.split("\n");
            } else {
                System.out.println("无法获取翻译歌词");
            }
        }
        // 处理歌词数据
        for (int i = 0; i < Objects.requireNonNull(mLrc).length; i++) {

            if (mLrc[i].contains("][")) {
                String[] temp = mLrc[i].split("]");
                if (mLrc[i].charAt(mLrc[i].length() - 1) != ']') {
                    for (int j = 0; j < temp.length - 1; j++) {
                        mLrcList.add(temp[j] + "]" + temp[temp.length - 1]);
                    }
                } else {
                    for (int j = 0; j < temp.length - 1; j++) {
                        mLrcList.add(temp[j] + "] ");
                    }
                }
            } else {
                mLrcList.add(mLrc[i]);
            }
        }
    }

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

    public static void writeAndSaveLyricFile(File mp3File) {
        if (mp3File.exists()) {
            String encoding = "utf-8";
            String mp3FileName = mp3File.getName();
            String fileName = getSongName(mp3FileName) + MFD.LRC;
            File lyricsFile = new File(MFD.UPLOADFILEPATH + fileName);
            List<String> list = new ArrayList<>();
            try {

                boolean file1 = lyricsFile.isFile();
                boolean exists = lyricsFile.exists();
                if (lyricsFile.isFile() && lyricsFile.exists()) { // 判断文件是否存在
                    InputStreamReader read = new InputStreamReader(
                            Files.newInputStream(lyricsFile.toPath()), encoding);// 考虑到编码格式
                    BufferedReader bufferedReader = new BufferedReader(read);
                    String lineTxt = null;
                    while ((lineTxt = bufferedReader.readLine()) != null) {
                        list.add(lineTxt);
                        // 移除歌词中的空行
                        list.remove("");
                    }
                    bufferedReader.close();
                    read.close();
                }
                Mp3File file = new Mp3File(mp3File);
                ID3v2 id3v2Tag = file.getId3v2Tag();
                id3v2Tag.setLyrics(list.toString());
//                // 删除处理后的数据
//                if (deleteFile(MFD.FILEPATH + fileName + MFD.FLAC)) System.out.println("Successfully Deleted");
                // 生成新的数据
                file.save(MFD.UPLOADFILEPATH + mp3FileName);
//                if (deleteFile(mp3File.getAbsolutePath())) System.out.println("Successfully Deleted");
            } catch (IOException | UnsupportedTagException | InvalidDataException | NotSupportedException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        System.out.println("要删除的文件所在位置: " + filePath);
        if (file.exists()) return file.delete();
        else {
            System.out.println("Failed To Delete");
            return false;
        }
    }

    public static boolean deleteFile(File file) {
        System.out.println("要删除的文件所在位置: " + file.getAbsolutePath());
        if (file.exists()) return file.delete();
        else {
            System.out.println("Failed To Delete");
            return false;
        }
    }

    public static String getSongName(String fileName) {
        if (!StringUtils.isNullOrEmpty(fileName)) return fileName.substring(0, fileName.lastIndexOf("."));
        return "传入的文件名为空";
    }

    @SneakyThrows
    public static void genLyricFile(List<String> list, String songName) {
        if (!CollectionUtils.isEmpty(list)){
            File file = new File(MFD.LYRICPATH,songName+MFD.LRC);
            if (!file.exists()){
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file);

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



    /*public static void main(String[] args) {
     *//*try {
            Process proc = Runtime.getRuntime().exec("python3 D:\\IDEAproject\\molusic\\src\\main\\resources\\pythonScript\\ncm2mp3.py");// 执行py文件
            //用输入输出流来截取结果
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
            proc.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*//*

        PythonInterpreter interpreter = new PythonInterpreter();
        interpreter.execfile("D:\\IDEAproject\\molusic\\src\\main\\resources\\pythonScript\\ncm2mp3.py");
    }*/
}
