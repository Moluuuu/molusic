package com.molu.audiofile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.molu.entity.MlcMusic;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface MlcMusicMapper extends BaseMapper<MlcMusic> {
    // 查询全部歌曲信息
    List<MlcMusic> musicQueryAll();

    // 根据 id查询歌曲信息
    MlcMusic musicQueryById(Integer id);

    // 歌曲搜索，模糊查询
    // 条件有 歌曲名称，专辑名称，歌手名称
    List<MlcMusic> musicSearch(String keywords);
}
