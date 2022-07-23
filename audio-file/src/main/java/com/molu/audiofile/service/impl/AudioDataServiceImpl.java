package com.molu.audiofile.service.impl;

import com.molu.audiofile.mapper.MlcMusicMapper;
import com.molu.audiofile.service.AudioDataService;
import com.molu.entity.MlcMusic;
import com.molu.utils.DataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class AudioDataServiceImpl implements AudioDataService {

    @Autowired
    MlcMusicMapper mlcMusicMapper;

    public boolean saveFile(MlcMusic music) {
        // 数据库操作，先查一下数据库中有没有该音乐文件的数据
        List<MlcMusic> mlcMusics = mlcMusicMapper.selectByMap(DataUtils.selectMap("file_name", music.getFileName()));
        // 没有就执行插入
        if (mlcMusics.isEmpty()) {
            mlcMusicMapper.insert(music);
            return true;
        }
        // 如果存在就进行比较，如果一样就跳过，否则 执行更新操作
        MlcMusic mlcMusic = mlcMusics.get(0);
        // 这里的操作是因为 id是随机产生的， creteTime是当前时间
        // 所以二者不可能相同 赋值后二者相同 如果其他数据也相同则不会通过判断
        music.setId(mlcMusic.getId());
        mlcMusic.setCreateTime(music.getCreateTime());
        if (!Objects.equals(mlcMusic, music)) {
            mlcMusicMapper.updateById(music);
            return true;
        }
        return false;
    }
}
