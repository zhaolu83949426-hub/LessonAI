package com.opensprout.lessonai.model.llm;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LlmConfigOptionRespVO {

    private Long id;

    private String name;

    private String modelCode;

    private Boolean isDefault;

}
