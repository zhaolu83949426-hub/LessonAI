package com.opensprout.lessonai.model.session;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatSessionRespVO {

    private Long id;

    private String title;

    private Long templateId;

    private String templateName;

    private Long llmConfigId;

    private String llmConfigName;

    private Long currentResultId;

    private String latestQuestion;

    private String resultStatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
