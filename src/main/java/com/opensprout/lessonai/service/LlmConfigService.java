package com.opensprout.lessonai.service;

import com.opensprout.lessonai.domain.entity.LlmConfigDO;
import com.opensprout.lessonai.model.llm.LlmConfigOptionRespVO;

import java.util.List;

public interface LlmConfigService {

    List<LlmConfigOptionRespVO> listOptions();

    LlmConfigOptionRespVO getDefaultOption();

    LlmConfigDO getEnabledById(Long id);

}
