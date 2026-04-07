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

    private String templateName;

    private Long llmConfigId;

    private String modelName;

    private String topic;

    private String inputPayload;

    private String finalPrompt;

    private String resultContent;

    private String editedContent;

    private Integer feedbackRating;

    private String feedbackLevel;

    private String feedbackComment;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
