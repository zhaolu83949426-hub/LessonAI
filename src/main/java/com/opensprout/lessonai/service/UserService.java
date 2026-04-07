package com.opensprout.lessonai.service;

import com.opensprout.lessonai.domain.entity.UserDO;
import com.opensprout.lessonai.model.auth.AuthLoginReqVO;
import com.opensprout.lessonai.model.auth.AuthRegisterReqVO;
import com.opensprout.lessonai.model.auth.AuthTokenRespVO;
import com.opensprout.lessonai.model.auth.UserPasswordUpdateReqVO;
import com.opensprout.lessonai.model.auth.UserProfileRespVO;
import com.opensprout.lessonai.model.auth.UserProfileUpdateReqVO;

public interface UserService {

    AuthTokenRespVO register(AuthRegisterReqVO reqVO);

    AuthTokenRespVO login(AuthLoginReqVO reqVO);

    UserProfileRespVO getCurrentUserProfile();

    UserProfileRespVO updateProfile(UserProfileUpdateReqVO reqVO);

    void updatePassword(UserPasswordUpdateReqVO reqVO);

    UserDO getById(Long id);

}
