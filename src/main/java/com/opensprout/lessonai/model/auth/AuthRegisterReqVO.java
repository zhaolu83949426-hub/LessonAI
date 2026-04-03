package com.opensprout.lessonai.model.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRegisterReqVO {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 32, message = "用户名长度不能超过32个字符")
    private String account;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 32, message = "昵称长度不能超过32个字符")
    private String nickname;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度必须在6到32个字符之间")
    private String password;

}
