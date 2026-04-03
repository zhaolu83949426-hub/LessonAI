package com.opensprout.lessonai.model.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthLoginReqVO {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 32, message = "用户名长度不能超过32个字符")
    private String account;

    @NotBlank(message = "密码不能为空")
    @Size(max = 32, message = "密码长度不能超过32个字符")
    private String password;

}
