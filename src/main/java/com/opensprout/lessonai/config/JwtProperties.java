package com.opensprout.lessonai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "lesson-ai.jwt")
public class JwtProperties {

    private String secret;

    private long expireSeconds;

}
