package com.opensprout.lessonai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "lesson-ai.llm")
public class LlmProperties {

    private String defaultName;

    private String providerType;

    private String baseUrl;

    private String apiKey;

    private String modelCode;

}
