package com.opensprout.lessonai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.opensprout.lessonai.common.exception.BusinessException;
import com.opensprout.lessonai.domain.entity.PromptTemplateDO;
import com.opensprout.lessonai.mapper.PromptTemplateMapper;
import com.opensprout.lessonai.model.template.PromptTemplateRespVO;
import com.opensprout.lessonai.model.template.PromptTemplateSaveReqVO;
import com.opensprout.lessonai.security.SecurityUtils;
import com.opensprout.lessonai.service.PromptTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PromptTemplateServiceImpl implements PromptTemplateService {

    private final PromptTemplateMapper promptTemplateMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(PromptTemplateSaveReqVO reqVO) {
        Long userId = SecurityUtils.getCurrentUserId();
        PromptTemplateDO template = new PromptTemplateDO();
        template.setUserId(userId);
        template.setName(reqVO.getName().trim());
        template.setContent(reqVO.getContent().trim());
        promptTemplateMapper.insert(template);
        return template.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, PromptTemplateSaveReqVO reqVO) {
        PromptTemplateDO template = requireOwnedTemplate(id);
        template.setName(reqVO.getName().trim());
        template.setContent(reqVO.getContent().trim());
        promptTemplateMapper.updateById(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        PromptTemplateDO template = requireOwnedTemplate(id);
        promptTemplateMapper.deleteById(template.getId());
    }

    @Override
    public PromptTemplateRespVO get(Long id) {
        return toResp(requireOwnedTemplate(id));
    }

    @Override
    public List<PromptTemplateRespVO> list() {
        return promptTemplateMapper.selectList(new LambdaQueryWrapper<PromptTemplateDO>()
                        .eq(PromptTemplateDO::getUserId, SecurityUtils.getCurrentUserId())
                        .orderByDesc(PromptTemplateDO::getUpdatedAt))
                .stream()
                .map(this::toResp)
                .toList();
    }

    @Override
    public PromptTemplateDO getByIdAndUserId(Long id, Long userId) {
        return promptTemplateMapper.selectOne(new LambdaQueryWrapper<PromptTemplateDO>()
                .eq(PromptTemplateDO::getId, id)
                .eq(PromptTemplateDO::getUserId, userId)
                .last("LIMIT 1"));
    }

    private PromptTemplateDO requireOwnedTemplate(Long id) {
        PromptTemplateDO template = getByIdAndUserId(id, SecurityUtils.getCurrentUserId());
        if (template == null) {
            throw new BusinessException("模板不存在");
        }
        return template;
    }

    private PromptTemplateRespVO toResp(PromptTemplateDO template) {
        return PromptTemplateRespVO.builder()
                .id(template.getId())
                .name(template.getName())
                .content(template.getContent())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }

}
