package com.opensprout.lessonai.model.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileUpdateReqVO {

    @NotBlank(message = "昵称不能为空")
    @Size(max = 32, message = "昵称长度不能超过32个字符")
    private String nickname;

    private Long defaultTemplateId;

    @Size(max = 50, message = "风格偏好长度不能超过50个字符")
    private String defaultStyle;
}
