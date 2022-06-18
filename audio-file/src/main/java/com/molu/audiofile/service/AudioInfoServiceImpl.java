package com.molu.audiofile.service;

import com.molu.audiofile.dictionary.MFD;
import com.molu.audiofile.utils.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;


@Service
public class AudioInfoServiceImpl implements AudioInfoService {

    @Override
    public Map<String, List<String>> getTempInfo() {
        File music = new File(MFD.FILEPATH);
        File lyric = new File(MFD.UPLOADFILEPATH);
        File upload = new File(MFD.LYRICPATH);
        return FileUtils.getDirFiles(music, lyric, upload);
    }
}
