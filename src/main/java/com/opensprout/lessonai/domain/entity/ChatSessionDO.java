package com.opensprout.lessonai.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("chat_sessions")
public class ChatSessionDO extends BaseDO {

    /**
     * 会话主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 关联模板ID
     */
    private Long templateId;

    /**
     * 关联模型配置ID
     */
    private Long llmConfigId;

    /**
     * 当前教案结果ID
     */
    private Long currentResultId;

}
