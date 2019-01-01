package com.popush.triela.common.auth;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserDaoMapper userDaoMapper;

    void registUser(@NonNull int gitHubId) {
        if (userDaoMapper.select(gitHubId) == null) {
            userDaoMapper.insert(UserDao.builder().gitHubId(gitHubId).build());
        }
    }

}
