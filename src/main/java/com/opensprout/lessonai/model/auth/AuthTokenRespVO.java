package com.opensprout.lessonai.model.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthTokenRespVO {

    private String token;

    private UserProfileRespVO user;

}
