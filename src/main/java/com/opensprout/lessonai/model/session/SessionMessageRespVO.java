package com.opensprout.lessonai.model.session;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SessionMessageRespVO {

    private Long id;

    private String role;

    private String content;

    private Long lessonRecordId;

    private LocalDateTime createdAt;

}
