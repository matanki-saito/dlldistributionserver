package com.popush.triela.common.auth;

import lombok.NonNull;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserDaoMapper {
    @Insert("INSERT INTO user (github_id, role) VALUES (#{gitHubId}, #{role})")
    void insert(@NonNull UserDao userDao);

    @Select("SELECT * FROM user WHERE github_id = #{gitHubId}")
    @Results({
            @Result(property = "gitHubId", column = "github_id")
    })
    UserDao select(int gitHubId);
}
