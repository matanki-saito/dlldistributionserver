package com.popush.triela.db;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.NonNull;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.popush.triela.common.db.ExeEntity;
import com.popush.triela.common.db.ExeSelectCondition;

@Mapper
public interface ExeMapper {
    @Select("""
            SELECT *
            FROM `exe`
            WHERE `id` = #{exeId}
            LIMIT 1
            """)
    Optional<ExeEntity> selectById(Integer exeId);

    @Select("""
            <script>
            SELECT `github_repo_id`
            FROM `exe`
            <where>
                `id` IN 
                <foreach item="exeId" collection="exeIds" open="(" separator="," close=")">
                 #{exeId}
                </foreach>
            </where>
            GROUP BY `github_repo_id`
            </script>
            """)
    Set<Integer> findGitHubIdSetByIds(@Param("exeIds") Set<Integer> exeIds);

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
    void insert(@NonNull ExeEntity exeDao);

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
    void update(
            @Param("condition") @NonNull ExeSelectCondition condition,
            @Param("data") ExeEntity exeDao
    );

    @Delete("""
            <script>
            DELETE FROM exe
            <where>
                id = #{id}
            </where>
            </script>
            """)
    void delete(@NonNull Integer id);

    @Results(id = "exe_entity", value = {
            @Result(property = "distributionAssetId", column = "distribution_asset_id"),
            @Result(property = "gitHubRepoId", column = "github_repo_id")
    })
    @Select("""
            <script>
            SELECT *
            FROM `exe`
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
                <if test="condition.autoUpdate != null">
                    AND auto_update = #{condition.autoUpdate}
                </if>
            </where>
            ORDER BY `version` DESC,`phase` ASC 
            LIMIT #{offset}, #{requestCount}
            </script>
            """)
    List<ExeEntity> selectByCondition(@Param("condition") ExeSelectCondition condition,
                                      @Param("offset") Long offset,
                                      @Param("requestCount") Integer requestCount);

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM (
                SELECT id
                FROM `exe`
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
                    <if test="condition.autoUpdate != null">
                        AND auto_update = #{condition.autoUpdate}
                    </if>
                </where>
                LIMIT #{maxLimit}
            ) AS selectByCondition
            </script>
            """)
    Integer countByConditionWithLimit(@Param("condition") ExeSelectCondition condition,
                                      @Param("maxLimit") Long maxLimit);
}
