package com.molu.processing.utils;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.*;

public class LyricTest {

    @SneakyThrows
    @Test
    public void test(){
        String songName = "青葉市子-外は戦場だよ";
        boolean translate = true;
        Map<String, String> ids = MusicUtils.getMusicIdByName(songName);
        String id = null;
        if (!ids.isEmpty()){
            id = ids.get("0");
            System.out.println(id);
        }
        if (translate){
            Map<String, List<String>> lyricMap = MusicUtils.getLyric(id, translate);
            if (!lyricMap.isEmpty()){
                List<String> list = MusicUtils.processingLyric(lyricMap);
//                MusicUtils.genAndSaveLyricFile(list,songName);
            }
        }

    }

}
