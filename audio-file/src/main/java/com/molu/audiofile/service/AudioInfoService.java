package com.molu.audiofile.service;

import com.molu.entity.MlcMusic;

import java.util.List;
import java.util.Map;

public interface AudioInfoService {
    //
    Map<String, List<String>> getTempInfo();

    public boolean saveFileInfo(MlcMusic music);
}
