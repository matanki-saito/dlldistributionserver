package com.popush.triela.db

import com.popush.triela.common.db.ExeEntity
import com.popush.triela.common.db.ExeSelectCondition
import lombok.NonNull
import org.apache.ibatis.annotations.*
import java.util.*

@Mapper
interface ExeMapper {
    @Select("""
        SELECT *
        FROM `exe`
        WHERE `id` = #{exeId}
        LIMIT 1
    """)
    fun selectById(exeId: Int): Optional<ExeEntity>

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
        )
    """)
    fun insert(@NonNull exeDao: ExeEntity)

    @Update("""
        <script>
            UPDATE `exe`
            <set>
              <if test="data.id != null">id=#{data.id},</if>
              <if test="data.gitHubRepoId != null">github_repo_id=#{data.gitHubRepoId},</if>
              <if test="data.md5 != null">md5=#{data.md5},</if>
              <if test="data.version != null">version=#{data.version},</if>
              <if test="data.description != null">description=#{data.description},</if>
              <if test="data.distributionAssetId != null">distribution_asset_id=#{data.distributionAssetId},</if>
              <if test="data.phase != null">phase=#{data.phase},</if>
              <if test="data.autoUpdate != null">auto_update=#{data.autoUpdate}</if>
            </set>
            <where>
                <if test="condition.gitHubRepoId != null">
                    AND github_repo_id = #{condition.gitHubRepoId}
                </if>
                <if test="condition.md5 != null">
                    AND md5 = #{condition.md5}
                </if>
                <if test="condition.phase != null">
                    AND phase = #{condition.phase}
                </if>
                <if test="condition.id != null">
                    AND id = #{condition.id}
                </if>
            </where>
        </script>
    """)
    fun update(
            @Param("condition") @NonNull condition: ExeSelectCondition,
            @Param("data") exeDao: ExeEntity
    );

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
                <if test="autoUpdate != null">
                    AND auto_update = #{autoUpdate}
                </if>
            </where>
            ORDER BY `version` DESC,`phase` ASC 
            LIMIT 100
        </script>
    """)
    fun list(@NonNull condition: ExeSelectCondition): List<ExeEntity>
}
