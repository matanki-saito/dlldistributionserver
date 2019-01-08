package com.popush.triela.common.DB;

import lombok.NonNull;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ExeDaoMapper {
    @Insert("INSERT INTO exe (id,md5, version,github_repo_id, description, distribution_asset_id) " +
            "VALUES (#{id}, #{md5},#{version}, #{gitHubRepoId}, #{description}, #{distributionAssetId})" +
            "ON DUPLICATE KEY UPDATE distribution_asset_id = #{distributionAssetId} ")
    void upsert(@NonNull ExeDao exeDao);

    @Results({
            @Result(property = "distributionAssetId", column = "distribution_asset_id"),
            @Result(property = "gitHubRepoId", column = "github_repo_id")
    })
    @Select("<script>" +
            "SELECT * FROM exe" +
            "<where>" +
            "<if test=\"gitHubRepoId != null\">" +
            "AND github_repo_id = #{gitHubRepoId} " +
            "</if>" +
            "<if test=\"md5 != null\">" +
            "AND md5 = #{md5} " +
            "</if>" +
            "</where>" +
            "</script>")
    List<ExeDao> list(@NonNull ExeSelectCondition condition);

}
