package com.opensprout.lessonai.model.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPasswordUpdateReqVO {

    @NotBlank(message = "原密码不能为空")
    @Size(max = 32, message = "原密码长度不能超过32个字符")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 32, message = "新密码长度必须在6到32个字符之间")
    private String newPassword;
}
