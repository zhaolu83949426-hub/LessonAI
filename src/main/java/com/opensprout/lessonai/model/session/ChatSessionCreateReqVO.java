package com.opensprout.lessonai.model.session;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatSessionCreateReqVO {

    @NotNull(message = "模板不能为空")
    private Long templateId;

    @NotNull(message = "模型不能为空")
    private Long llmConfigId;

    @NotBlank(message = "会话标题不能为空")
    @Size(max = 100, message = "会话标题长度不能超过100个字符")
    private String title;

}
