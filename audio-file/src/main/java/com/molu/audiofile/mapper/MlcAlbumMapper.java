package com.molu.audiofile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.molu.entity.MlcAlbum;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface MlcAlbumMapper extends BaseMapper<MlcAlbum> {
    // 查询全部歌曲信息
    List<MlcAlbum> albumQueryAll();

    // 根据 id查询专辑信息
    MlcAlbum albumQueryAllById(Integer id);
}
