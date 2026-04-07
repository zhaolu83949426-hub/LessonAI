package com.opensprout.lessonai.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.opensprout.lessonai.config.mybatis.JsonbStringTypeHandler;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.JdbcType;

@Getter
@Setter
@TableName(value = "lesson_records", autoResultMap = true)
public class LessonRecordDO extends BaseDO {

    /**
     * 教案记录主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 所属会话ID
     */
    private Long sessionId;

    /**
     * 使用模板ID
     */
    private Long templateId;

    /**
     * 使用模型配置ID
     */
    private Long llmConfigId;

    /**
     * 模型名称快照
     */
    private String modelName;

    /**
     * 教案主题
     */
    private String topic;

    /**
     * 输入参数快照
     */
    @TableField(value = "input_payload", typeHandler = JsonbStringTypeHandler.class, jdbcType = JdbcType.OTHER)
    private String inputPayload;

    /**
     * 最终发送给模型的Prompt
     */
    private String finalPrompt;

    /**
     * 模型原始生成结果
     */
    private String resultContent;

    /**
     * 用户编辑后的教案内容
     */
    private String editedContent;

    /**
     * 质量评分
     */
    private Integer feedbackRating;

    /**
     * 质量等级
     */
    private String feedbackLevel;

    /**
     * 质量反馈说明
     */
    private String feedbackComment;

}
