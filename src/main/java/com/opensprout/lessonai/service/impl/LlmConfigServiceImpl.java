package com.opensprout.lessonai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.opensprout.lessonai.common.exception.BusinessException;
import com.opensprout.lessonai.domain.entity.LlmConfigDO;
import com.opensprout.lessonai.mapper.LlmConfigMapper;
import com.opensprout.lessonai.model.llm.LlmConfigOptionRespVO;
import com.opensprout.lessonai.service.LlmConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LlmConfigServiceImpl implements LlmConfigService {

    private final LlmConfigMapper llmConfigMapper;

    @Override
    public List<LlmConfigOptionRespVO> listOptions() {
        return llmConfigMapper.selectList(new LambdaQueryWrapper<LlmConfigDO>()
                        .eq(LlmConfigDO::getEnabled, Boolean.TRUE)
                        .orderByAsc(LlmConfigDO::getSortOrder)
                        .orderByAsc(LlmConfigDO::getId))
                .stream()
                .map(this::toOption)
                .toList();
    }

    @Override
    public LlmConfigOptionRespVO getDefaultOption() {
        LlmConfigDO config = llmConfigMapper.selectOne(new LambdaQueryWrapper<LlmConfigDO>()
                .eq(LlmConfigDO::getEnabled, Boolean.TRUE)
                .eq(LlmConfigDO::getIsDefault, Boolean.TRUE)
                .last("LIMIT 1"));
        if (config == null) {
            throw new BusinessException("暂无默认模型");
        }
        return toOption(config);
    }

    @Override
    public LlmConfigDO getEnabledById(Long id) {
        LlmConfigDO config = llmConfigMapper.selectOne(new LambdaQueryWrapper<LlmConfigDO>()
                .eq(LlmConfigDO::getId, id)
                .eq(LlmConfigDO::getEnabled, Boolean.TRUE)
                .last("LIMIT 1"));
        if (config == null) {
            throw new BusinessException("模型不存在或未启用");
        }
        return config;
    }

    private LlmConfigOptionRespVO toOption(LlmConfigDO config) {
        return LlmConfigOptionRespVO.builder()
                .id(config.getId())
                .name(config.getName())
                .modelCode(config.getModelCode())
                .isDefault(config.getIsDefault())
                .build();
    }

}
