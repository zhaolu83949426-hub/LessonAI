package com.opensprout.lessonai.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.opensprout.lessonai.domain.entity.LlmConfigDO;
import com.opensprout.lessonai.mapper.LlmConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LlmConfigBootstrap implements CommandLineRunner {

    private static final int DEFAULT_SORT_ORDER = 0;

    private final LlmConfigMapper llmConfigMapper;
    private final LlmProperties llmProperties;

    @Override
    public void run(String... args) {
        LlmConfigDO config = llmConfigMapper.selectOne(new LambdaQueryWrapper<LlmConfigDO>()
                .eq(LlmConfigDO::getIsDefault, Boolean.TRUE)
                .eq(LlmConfigDO::getDeleted, Boolean.FALSE)
                .last("LIMIT 1"));
        if (config != null) {
            return;
        }
        config = new LlmConfigDO();
        config.setEnabled(Boolean.TRUE);
        config.setIsDefault(Boolean.TRUE);
        config.setSortOrder(DEFAULT_SORT_ORDER);
        applyDefaultConfig(config);
        llmConfigMapper.insert(config);
    }

    private void applyDefaultConfig(LlmConfigDO config) {
        config.setName(llmProperties.getDefaultName());
        config.setProviderType(llmProperties.getProviderType());
        config.setBaseUrl(llmProperties.getBaseUrl());
        config.setApiKey(llmProperties.getApiKey());
        config.setModelCode(llmProperties.getModelCode());
    }

}
