package com.opensprout.lessonai.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("session_messages")
public class SessionMessageDO extends BaseDO {

    /**
     * 消息主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属会话ID
     */
    private Long sessionId;

    /**
     * 消息角色
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 关联教案记录ID
     */
    private Long lessonRecordId;

}
