package com.opensprout.lessonai.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("users")
public class UserDO extends BaseDO {

    /**
     * 用户主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 登录账号
     */
    private String account;

    /**
     * 密码哈希
     */
    private String passwordHash;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 默认模板ID
     */
    private Long defaultTemplateId;

    /**
     * 默认教案风格偏好
     */
    private String defaultStyle;

}
