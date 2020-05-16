package com.popush.triela.db

import com.popush.triela.common.db.FileDto
import com.popush.triela.common.db.FileSelectCondition
import lombok.NonNull
import org.apache.ibatis.annotations.*
import java.util.*

@Mapper
interface FileDao {
    @Insert("""
        INSERT INTO file (
            md5,
            data,
            data_url,
            data_size,
            asset_id
        )
        VALUES (
            #{md5},
            #{data},
            #{dataUrl},
            #{dataSize},
            #{assetId}
        )
        ON DUPLICATE KEY UPDATE asset_id = asset_id
    """)
    fun upsert(@NonNull fileDao: FileDto)

    @Results(id = "filedao", value = [
        Result(property = "assetId", column = "asset_id"),
        Result(property = "dataSize", column = "data_size"),
        Result(property = "dataUrl", column = "data_url")
    ])
    @Select("SELECT * FROM file WHERE asset_id = #{assetId}")
    fun selectByAssetId(assetId: Int): Optional<FileDto>

    @Results(id = "filedao2", value = [
        Result(property = "assetId", column = "asset_id"),
        Result(property = "dataSize", column = "data_size"),
        Result(property = "dataUrl", column = "data_url")
    ])
    @Select("""
        <script>
            SELECT b.* FROM exe ExeDao
            JOIN file b ON ExeDao.distribution_asset_id = b.asset_id
            <where>
                <if test="distributedExeMd5 != null">
                    AND ExeDao.md5 = #{distributedExeMd5}
                </if>
                <if test="md5 != null">
                    AND b.md5 = #{md5}
                </if>
                <if test="gitHubRepoId != null">
                    AND ExeDao.github_repo_id = #{gitHubRepoId}
                </if>
                <if test="phase != null">
                    AND ExeDao.phase = #{phase}
                </if>
            </where>
            ORDER BY ExeDao.version DESC
        </script>
    """)
    fun list(@NonNull condition: FileSelectCondition): List<FileDto>
}
