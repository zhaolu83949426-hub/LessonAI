package com.opensprout.lessonai.model.lesson;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LessonGenerateReqVO {

    @NotNull(message = "会话不能为空")
    private Long sessionId;

    @NotBlank(message = "主题不能为空")
    @Size(max = 100, message = "主题长度不能超过100个字符")
    private String topic;

    @NotBlank(message = "输入内容不能为空")
    @Size(max = 5000, message = "输入内容长度不能超过5000个字符")
    private String userMessage;

}
