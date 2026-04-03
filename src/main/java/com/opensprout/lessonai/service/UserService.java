package com.opensprout.lessonai.service;

import com.opensprout.lessonai.domain.entity.UserDO;
import com.opensprout.lessonai.model.auth.AuthLoginReqVO;
import com.opensprout.lessonai.model.auth.AuthRegisterReqVO;
import com.opensprout.lessonai.model.auth.AuthTokenRespVO;
import com.opensprout.lessonai.model.auth.UserProfileRespVO;

public interface UserService {

    AuthTokenRespVO register(AuthRegisterReqVO reqVO);

    AuthTokenRespVO login(AuthLoginReqVO reqVO);

    UserProfileRespVO getCurrentUserProfile();

    UserDO getById(Long id);

}
