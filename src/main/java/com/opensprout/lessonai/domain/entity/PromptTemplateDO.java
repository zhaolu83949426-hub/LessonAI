package com.opensprout.lessonai.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("prompt_templates")
public class PromptTemplateDO extends BaseDO {

    /**
     * 模板主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模板内容
     */
    private String content;

    /**
     * 模板分类
     */
    private String category;

    /**
     * 模板标签，逗号分隔
     */
    private String tags;

}
