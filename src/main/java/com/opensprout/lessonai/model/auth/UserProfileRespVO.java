package com.opensprout.lessonai.model.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserProfileRespVO {

    private Long id;

    private String account;

    private String nickname;

    private Long defaultTemplateId;

    private String defaultStyle;

}
