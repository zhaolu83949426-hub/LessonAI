package com.opensprout.lessonai.model.template;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PromptTemplateRespVO {

    private Long id;

    private String name;

    private String content;

    private String category;

    private String tags;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
