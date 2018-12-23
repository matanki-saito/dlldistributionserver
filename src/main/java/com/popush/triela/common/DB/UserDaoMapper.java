package com.popush.triela.common.DB;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserDaoMapper {
    @Insert("INSERT INTO user (github_id, role) VALUES (#{gitHubId}, #{role})")
    @Options(useGeneratedKeys = true)
    void insert(UserDao userDao);

    @Select("SELECT * FROM user WHERE github_id = #{gitHubId}")
    UserDao select(String gitHubId);
}
