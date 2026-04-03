package com.opensprout.lessonai.model.lesson;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class LessonRecordRespVO {

    private Long id;

    private Long sessionId;

    private Long templateId;

    private Long llmConfigId;

    private String modelName;

    private String topic;

    private String inputPayload;

    private String finalPrompt;

    private String resultContent;

    private String editedContent;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
