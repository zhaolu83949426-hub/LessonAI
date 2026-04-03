package com.opensprout.lessonai.model.lesson;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LessonRecordUpdateReqVO {

    @NotBlank(message = "教案内容不能为空")
    @Size(max = 20000, message = "教案内容长度不能超过20000个字符")
    private String editedContent;

}
