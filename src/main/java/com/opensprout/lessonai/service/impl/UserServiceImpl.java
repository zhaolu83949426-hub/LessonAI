package com.opensprout.lessonai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.opensprout.lessonai.common.exception.BusinessException;
import com.opensprout.lessonai.domain.entity.UserDO;
import com.opensprout.lessonai.mapper.UserMapper;
import com.opensprout.lessonai.model.auth.AuthLoginReqVO;
import com.opensprout.lessonai.model.auth.AuthRegisterReqVO;
import com.opensprout.lessonai.model.auth.AuthTokenRespVO;
import com.opensprout.lessonai.model.auth.UserPasswordUpdateReqVO;
import com.opensprout.lessonai.model.auth.UserProfileRespVO;
import com.opensprout.lessonai.model.auth.UserProfileUpdateReqVO;
import com.opensprout.lessonai.security.JwtTokenProvider;
import com.opensprout.lessonai.security.SecurityUtils;
import com.opensprout.lessonai.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthTokenRespVO register(AuthRegisterReqVO reqVO) {
        UserDO existUser = userMapper.selectOne(new LambdaQueryWrapper<UserDO>()
                .eq(UserDO::getAccount, reqVO.getAccount())
                .last("LIMIT 1"));
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }
        UserDO user = new UserDO();
        user.setAccount(reqVO.getAccount().trim());
        user.setNickname(reqVO.getNickname().trim());
        user.setPasswordHash(passwordEncoder.encode(reqVO.getPassword()));
        userMapper.insert(user);
        return buildTokenResp(user);
    }

    @Override
    public AuthTokenRespVO login(AuthLoginReqVO reqVO) {
        UserDO user = userMapper.selectOne(new LambdaQueryWrapper<UserDO>()
                .eq(UserDO::getAccount, reqVO.getAccount().trim())
                .last("LIMIT 1"));
        if (user == null || !passwordEncoder.matches(reqVO.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("用户名或密码错误");
        }
        return buildTokenResp(user);
    }

    @Override
    public UserProfileRespVO getCurrentUserProfile() {
        UserDO user = SecurityUtils.getCurrentUser();
        return toProfile(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserProfileRespVO updateProfile(UserProfileUpdateReqVO reqVO) {
        UserDO user = SecurityUtils.getCurrentUser();
        user.setNickname(reqVO.getNickname().trim());
        user.setDefaultTemplateId(reqVO.getDefaultTemplateId());
        user.setDefaultStyle(normalizeStyle(reqVO.getDefaultStyle()));
        userMapper.updateById(user);
        return toProfile(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(UserPasswordUpdateReqVO reqVO) {
        UserDO user = SecurityUtils.getCurrentUser();
        if (!passwordEncoder.matches(reqVO.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException("原密码错误");
        }
        user.setPasswordHash(passwordEncoder.encode(reqVO.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    public UserDO getById(Long id) {
        return userMapper.selectById(id);
    }

    private AuthTokenRespVO buildTokenResp(UserDO user) {
        String token = jwtTokenProvider.createToken(user.getId(), user.getAccount());
        return new AuthTokenRespVO(token, toProfile(user));
    }

    private UserProfileRespVO toProfile(UserDO user) {
        return new UserProfileRespVO(
                user.getId(),
                user.getAccount(),
                user.getNickname(),
                user.getDefaultTemplateId(),
                user.getDefaultStyle()
        );
    }

    private String normalizeStyle(String style) {
        if (style == null) {
            return "";
        }
        return style.trim();
    }

}
