package com.popush.triela.db

import com.popush.triela.common.db.ExeDto
import com.popush.triela.common.db.ExeSelectCondition
import lombok.NonNull
import org.apache.ibatis.annotations.*

@Mapper
interface ExeDao {
    @Insert("""
        INSERT INTO exe
        (
            id,
            md5,
            version,
            github_repo_id,
            description,
            distribution_asset_id,
            phase
        )
        VALUES
        (
            #{id},
            #{md5},
            #{version},
            #{gitHubRepoId},
            #{description},
            #{distributionAssetId},
            #{phase}
        );
        ON DUPLICATE KEY UPDATE distribution_asset_id = #{distributionAssetId}
    """)
    fun upsert(@NonNull exeDao: ExeDto)

    @Delete("""
        <script>
            DELETE FROM exe
            <where>
                id = #{id}
            </where>
        </script>
    """)
    fun delete(@NonNull id: Int)

    @Results(
            Result(property = "distributionAssetId", column = "distribution_asset_id"),
            Result(property = "gitHubRepoId", column = "github_repo_id")
    )
    @Select("""
        <script>
            SELECT * FROM exe
            <where>
                <if test="gitHubRepoId != null">
                    AND github_repo_id = #{gitHubRepoId}
                </if>
                <if test="md5 != null">
                    AND md5 = #{md5}
                </if>
                <if test="phase != null">
                    AND phase = #{phase}
                </if>
                <if test="id != null">
                    AND id = #{id}
                </if>
            </where>
        </script>
    """)
    fun list(@NonNull condition: ExeSelectCondition): List<ExeDto>
}