package com.opensprout.lessonai.controller;

import com.opensprout.lessonai.common.model.ApiResponse;
import com.opensprout.lessonai.model.auth.AuthLoginReqVO;
import com.opensprout.lessonai.model.auth.AuthRegisterReqVO;
import com.opensprout.lessonai.model.auth.AuthTokenRespVO;
import com.opensprout.lessonai.model.auth.UserProfileRespVO;
import com.opensprout.lessonai.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ApiResponse<AuthTokenRespVO> register(@Valid @RequestBody AuthRegisterReqVO reqVO) {
        return ApiResponse.success(userService.register(reqVO));
    }

    @PostMapping("/login")
    public ApiResponse<AuthTokenRespVO> login(@Valid @RequestBody AuthLoginReqVO reqVO) {
        return ApiResponse.success(userService.login(reqVO));
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileRespVO> me() {
        return ApiResponse.success(userService.getCurrentUserProfile());
    }

}
