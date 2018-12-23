package com.popush.triela.common.DB;

import lombok.NonNull;
import org.apache.ibatis.annotations.*;

@Mapper
public interface FileDaoMapper {
    @Insert("INSERT INTO file (md5, data,asset_id) VALUES (#{md5}, #{data}, #{assetId}) ON DUPLICATE KEY UPDATE asset_id = asset_id")
    void upsert(@NonNull FileDao fileDao);

    @Results({
            @Result(property = "assetId", column = "asset_id")
    })
    @Select("SELECT * FROM file WHERE md5 = #{md5}")
    FileDao selectByMd5(@NonNull String md5);

    @Results({
            @Result(property = "assetId", column = "asset_id")
    })
    @Select("SELECT * FROM file WHERE asset_id = #{assetId}")
    FileDao selectByAssetId(int asset_id);
}
