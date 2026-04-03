package com.opensprout.lessonai.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("llm_configs")
public class LlmConfigDO extends BaseDO {

    /**
     * 模型配置主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 配置名称
     */
    private String name;

    /**
     * 提供商类型
     */
    private String providerType;

    /**
     * 接口基础地址
     */
    private String baseUrl;

    /**
     * 接口鉴权Key
     */
    private String apiKey;

    /**
     * 模型编码
     */
    private String modelCode;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 是否默认模型
     */
    private Boolean isDefault;

    /**
     * 排序值
     */
    private Integer sortOrder;

}
