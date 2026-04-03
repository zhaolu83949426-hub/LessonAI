package com.opensprout.lessonai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opensprout.lessonai.common.exception.BusinessException;
import com.opensprout.lessonai.domain.entity.ChatSessionDO;
import com.opensprout.lessonai.domain.entity.LessonRecordDO;
import com.opensprout.lessonai.domain.entity.LlmConfigDO;
import com.opensprout.lessonai.domain.entity.PromptTemplateDO;
import com.opensprout.lessonai.domain.entity.SessionMessageDO;
import com.opensprout.lessonai.mapper.LessonRecordMapper;
import com.opensprout.lessonai.mapper.SessionMessageMapper;
import com.opensprout.lessonai.model.lesson.LessonGenerateReqVO;
import com.opensprout.lessonai.model.lesson.LessonRecordRespVO;
import com.opensprout.lessonai.model.lesson.LessonRecordUpdateReqVO;
import com.opensprout.lessonai.security.SecurityUtils;
import com.opensprout.lessonai.service.ChatSessionService;
import com.opensprout.lessonai.service.LessonRecordService;
import com.opensprout.lessonai.service.LlmConfigService;
import com.opensprout.lessonai.service.PromptTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LessonRecordServiceImpl implements LessonRecordService {

    private static final String FIXED_OUTPUT_FORMAT = "请严格输出以下结构：活动名称、活动目标、活动准备、活动过程、延伸活动、注意事项。";

    private final LessonRecordMapper lessonRecordMapper;
    private final SessionMessageMapper sessionMessageMapper;
    private final ChatSessionService chatSessionService;
    private final PromptTemplateService promptTemplateService;
    private final LlmConfigService llmConfigService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LessonRecordRespVO generate(LessonGenerateReqVO reqVO) {
        Long userId = SecurityUtils.getCurrentUserId();
        ChatSessionDO session = chatSessionService.getByIdAndUserId(reqVO.getSessionId(), userId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        PromptTemplateDO template = promptTemplateService.getByIdAndUserId(session.getTemplateId(), userId);
        if (template == null) {
            throw new BusinessException("模板不存在");
        }
        LlmConfigDO llmConfig = llmConfigService.getEnabledById(session.getLlmConfigId());
        String finalPrompt = buildPrompt(template.getContent(), reqVO.getTopic().trim(), reqVO.getUserMessage().trim(), session.getId());
        String generatedResult = buildMockResult(reqVO.getTopic().trim(), reqVO.getUserMessage().trim());

        LessonRecordDO lessonRecord = new LessonRecordDO();
        lessonRecord.setUserId(userId);
        lessonRecord.setSessionId(session.getId());
        lessonRecord.setTemplateId(template.getId());
        lessonRecord.setLlmConfigId(llmConfig.getId());
        lessonRecord.setModelName(llmConfig.getModelCode());
        lessonRecord.setTopic(reqVO.getTopic().trim());
        lessonRecord.setInputPayload(toJson(Map.of(
                "topic", reqVO.getTopic().trim(),
                "userMessage", reqVO.getUserMessage().trim()
        )));
        lessonRecord.setFinalPrompt(finalPrompt);
        lessonRecord.setResultContent(generatedResult);
        lessonRecord.setEditedContent(generatedResult);
        lessonRecordMapper.insert(lessonRecord);

        saveMessage(session.getId(), "USER", reqVO.getUserMessage().trim(), lessonRecord.getId());
        saveMessage(session.getId(), "ASSISTANT", generatedResult, lessonRecord.getId());
        chatSessionService.updateCurrentResult(session.getId(), lessonRecord.getId());

        return toResp(lessonRecord);
    }

    @Override
    public List<LessonRecordRespVO> listBySessionId(Long sessionId) {
        ChatSessionDO session = chatSessionService.getByIdAndUserId(sessionId, SecurityUtils.getCurrentUserId());
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        return lessonRecordMapper.selectList(new LambdaQueryWrapper<LessonRecordDO>()
                        .eq(LessonRecordDO::getSessionId, sessionId)
                        .orderByAsc(LessonRecordDO::getCreatedAt)
                        .orderByAsc(LessonRecordDO::getId))
                .stream()
                .map(this::toResp)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LessonRecordRespVO update(Long id, LessonRecordUpdateReqVO reqVO) {
        LessonRecordDO record = lessonRecordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BusinessException("教案记录不存在");
        }
        record.setEditedContent(reqVO.getEditedContent().trim());
        lessonRecordMapper.updateById(record);
        return toResp(record);
    }

    private void saveMessage(Long sessionId, String role, String content, Long lessonRecordId) {
        SessionMessageDO message = new SessionMessageDO();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setLessonRecordId(lessonRecordId);
        sessionMessageMapper.insert(message);
    }

    private String buildPrompt(String templateContent, String topic, String userMessage, Long sessionId) {
        String context = sessionMessageMapper.selectList(new LambdaQueryWrapper<SessionMessageDO>()
                        .eq(SessionMessageDO::getSessionId, sessionId)
                        .orderByDesc(SessionMessageDO::getId)
                        .last("LIMIT 6"))
                .stream()
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .map(item -> item.getRole() + "：" + item.getContent())
                .reduce((left, right) -> left + "\n" + right)
                .orElse("无历史上下文");
        return "模板要求：\n" + templateContent + "\n\n"
                + FIXED_OUTPUT_FORMAT + "\n\n"
                + "本次主题：" + topic + "\n"
                + "老师输入：" + userMessage + "\n\n"
                + "历史上下文：\n" + context;
    }

    private String buildMockResult(String topic, String userMessage) {
        return "活动名称\n" + topic + "\n\n"
                + "活动目标\n"
                + "1. 引导幼儿围绕主题进行观察与表达。\n"
                + "2. 帮助幼儿在互动中完成一次完整活动体验。\n\n"
                + "活动准备\n"
                + "根据老师输入准备相关教具，结合需求：" + userMessage + "\n\n"
                + "活动过程\n"
                + "1. 导入主题，激发兴趣。\n"
                + "2. 组织互动与讲解。\n"
                + "3. 引导幼儿表达与总结。\n\n"
                + "延伸活动\n"
                + "鼓励幼儿在课后继续观察和分享。\n\n"
                + "注意事项\n"
                + "关注幼儿参与节奏，及时进行鼓励和引导。";
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new BusinessException("输入数据转换失败");
        }
    }

    private LessonRecordRespVO toResp(LessonRecordDO record) {
        return LessonRecordRespVO.builder()
                .id(record.getId())
                .sessionId(record.getSessionId())
                .templateId(record.getTemplateId())
                .llmConfigId(record.getLlmConfigId())
                .modelName(record.getModelName())
                .topic(record.getTopic())
                .inputPayload(record.getInputPayload())
                .finalPrompt(record.getFinalPrompt())
                .resultContent(record.getResultContent())
                .editedContent(record.getEditedContent())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }

}
