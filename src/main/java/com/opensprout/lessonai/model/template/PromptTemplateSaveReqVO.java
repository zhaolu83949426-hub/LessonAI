package com.opensprout.lessonai.model.template;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PromptTemplateSaveReqVO {

    @NotBlank(message = "模板名称不能为空")
    @Size(max = 50, message = "模板名称长度不能超过50个字符")
    private String name;

    @NotBlank(message = "模板内容不能为空")
    @Size(max = 10000, message = "模板内容长度不能超过10000个字符")
    private String content;

}
