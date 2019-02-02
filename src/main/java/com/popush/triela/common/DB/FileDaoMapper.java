package com.popush.triela.common.DB;

import lombok.NonNull;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FileDaoMapper {
    @Results({
            @Result(property = "assetId", column = "asset_id"),
            @Result(property = "dataSize", column = "data_size"),
            @Result(property = "dataUrl", column = "data_url")
    })
    @Insert("INSERT INTO file (md5, data, data_url, data_size, asset_id)" +
            "VALUES (#{md5}, #{data}, #{dataUrl}, #{dataSize}, #{assetId}) " +
            "ON DUPLICATE KEY UPDATE asset_id = asset_id"
    )
    void upsert(@NonNull FileDao fileDao);

    @Results({
            @Result(property = "assetId", column = "asset_id"),
            @Result(property = "dataSize", column = "data_size"),
            @Result(property = "dataUrl", column = "data_url")
    })
    @Select("SELECT * FROM file WHERE asset_id = #{assetId}")
    FileDao selectByAssetId(int asset_id);

    @Results({
            @Result(property = "assetId", column = "asset_id"),
            @Result(property = "dataSize", column = "data_size"),
            @Result(property = "dataUrl", column = "data_url")
    })
    @Select("<script>" +
            "SELECT b.* FROM exe a " +
            "LEFT JOIN file b ON a.distribution_asset_id = b.asset_id " +
            "WHERE a.md5 = #{distributedExeMd5} " +
            "<if test=\"md5 != null\">" +
            "AND b.md5 &lt;&gt; #{md5}" +
            "</if>" +
            "<if test=\"gitHubRepoId != null\">" +
            "AND a.github_repo_id = #{gitHubRepoId}" +
            "</if>" +
            "</script>")
    List<FileDao> list(
            @NonNull FileSelectCondition condition
    );
}
