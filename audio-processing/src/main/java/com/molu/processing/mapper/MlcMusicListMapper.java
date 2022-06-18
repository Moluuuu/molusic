package com.molu.processing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.molu.processing.pojo.MlcMusicList;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface MlcMusicListMapper extends BaseMapper<MlcMusicList> {
    // 查询全部歌单信息
    List<MlcMusicList> musicListQueryAll();

    // 根据 id查询歌单信息
    MlcMusicList musicListQueryById(Integer id);
}
