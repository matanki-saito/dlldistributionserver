package com.popush.triela.common.DB;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDao {
    private String gitHubId;
    private int role;
}
