package com.opensprout.lessonai.model.lesson;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LessonFeedbackUpdateReqVO {

    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    private Integer feedbackRating;

    @NotBlank(message = "质量等级不能为空")
    @Size(max = 32, message = "质量等级长度不能超过32个字符")
    private String feedbackLevel;

    @Size(max = 255, message = "反馈说明长度不能超过255个字符")
    private String feedbackComment;
}
