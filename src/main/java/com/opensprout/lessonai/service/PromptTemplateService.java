package com.opensprout.lessonai.service;

import com.opensprout.lessonai.domain.entity.PromptTemplateDO;
import com.opensprout.lessonai.model.template.PromptTemplateRespVO;
import com.opensprout.lessonai.model.template.PromptTemplateSaveReqVO;

import java.util.List;

public interface PromptTemplateService {

    Long create(PromptTemplateSaveReqVO reqVO);

    void update(Long id, PromptTemplateSaveReqVO reqVO);

    void delete(Long id);

    PromptTemplateRespVO get(Long id);

    List<PromptTemplateRespVO> list();

    PromptTemplateDO getByIdAndUserId(Long id, Long userId);

}
