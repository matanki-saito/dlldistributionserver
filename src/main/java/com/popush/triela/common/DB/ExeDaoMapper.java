package com.popush.triela.common.DB;

import lombok.NonNull;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ExeDaoMapper {
    @Insert("INSERT INTO exe (md5, version,product, description, distribution_asset_id) " +
            "VALUES (#{md5},#{version}, #{product}, #{description}, #{distributionAssetId})" +
            "ON DUPLICATE KEY UPDATE distribution_asset_id = #{distributionAssetId} ")
    void upsert(@NonNull ExeDao exeDao);

    @Results({
            @Result(property = "distributionAssetId", column = "distribution_asset_id")
    })
    @Select("SELECT * FROM exe WHERE md5 = #{md5}")
    ExeDao selectByMd5(@NonNull String md5);

    @Results({
            @Result(property = "distributionAssetId", column = "distribution_asset_id")
    })
    @Select("SELECT * FROM exe")
    List<ExeDao> list();
}
