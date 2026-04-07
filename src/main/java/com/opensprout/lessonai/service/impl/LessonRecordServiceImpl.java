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
import com.opensprout.lessonai.model.lesson.LessonFeedbackUpdateReqVO;
import com.opensprout.lessonai.model.lesson.LessonRecordRespVO;
import com.opensprout.lessonai.model.lesson.LessonRecordUpdateReqVO;
import com.opensprout.lessonai.security.SecurityUtils;
import com.opensprout.lessonai.service.ChatSessionService;
import com.opensprout.lessonai.service.LessonRecordService;
import com.opensprout.lessonai.service.LlmConfigService;
import com.opensprout.lessonai.service.PromptTemplateService;
import com.opensprout.lessonai.service.llm.LlmResponseClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class LessonRecordServiceImpl implements LessonRecordService {
    private static final String FIXED_OUTPUT_FORMAT = "请严格输出以下结构：活动名称、活动目标、活动准备、活动过程、延伸活动、注意事项。";
    private static final String INITIAL_GENERATION_MODE = "INITIAL";
    private static final String FOLLOW_UP_GENERATION_MODE = "FOLLOW_UP";
    private final LessonRecordMapper lessonRecordMapper;
    private final SessionMessageMapper sessionMessageMapper;
    private final ChatSessionService chatSessionService;
    private final PromptTemplateService promptTemplateService;
    private final LlmConfigService llmConfigService;
    private final LlmResponseClient llmResponseClient;
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
        String topic = reqVO.getTopic().trim();
        String userMessage = reqVO.getUserMessage().trim();
        String latestLessonContent = getLatestLessonContent(session);
        String generationMode = resolveGenerationMode(latestLessonContent);
        String finalPrompt = buildPrompt(template.getContent(), topic, userMessage, latestLessonContent);
        log.info("LLM request start, sessionId={}, llmConfigId={}, modelCode={}, topic={}, generationMode={}, prompt=\n{}",
                session.getId(),
                llmConfig.getId(),
                llmConfig.getModelCode(),
                topic,
                generationMode,
                finalPrompt);
        String generatedResult = llmResponseClient.request(llmConfig, finalPrompt, session.getId());
        log.info("LLM response received, sessionId={}, llmConfigId={}, modelCode={}, content=\n{}",
                session.getId(),
                llmConfig.getId(),
                llmConfig.getModelCode(),
                generatedResult);

        LessonRecordDO lessonRecord = new LessonRecordDO();
        lessonRecord.setUserId(userId);
        lessonRecord.setSessionId(session.getId());
        lessonRecord.setTemplateId(template.getId());
        lessonRecord.setLlmConfigId(llmConfig.getId());
        lessonRecord.setModelName(llmConfig.getModelCode());
        lessonRecord.setTopic(topic);
        lessonRecord.setInputPayload(toJson(Map.of(
                "generationMode", generationMode,
                "topic", topic,
                "userMessage", userMessage
        )));
        lessonRecord.setFinalPrompt(finalPrompt);
        lessonRecord.setResultContent(generatedResult);
        lessonRecord.setEditedContent(generatedResult);
        lessonRecordMapper.insert(lessonRecord);

        saveMessage(session.getId(), "USER", userMessage, lessonRecord.getId());
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LessonRecordRespVO updateFeedback(Long id, LessonFeedbackUpdateReqVO reqVO) {
        LessonRecordDO record = lessonRecordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BusinessException("教案记录不存在");
        }
        record.setFeedbackRating(reqVO.getFeedbackRating());
        record.setFeedbackLevel(reqVO.getFeedbackLevel().trim());
        record.setFeedbackComment(normalizeComment(reqVO.getFeedbackComment()));
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
    private String buildPrompt(String templateContent, String topic, String userMessage, String latestLessonContent) {
        if (isBlank(latestLessonContent)) {
            return buildInitialPrompt(templateContent, topic, userMessage);
        }
        return buildFollowUpPrompt(userMessage, latestLessonContent);
    }

    private String buildInitialPrompt(String templateContent, String topic, String userMessage) {
        return "你是一名专业的幼儿园教案助手，请根据以下模板要求和老师输入生成完整教案。\n\n"
                + "模板要求：\n" + templateContent + "\n\n"
                + FIXED_OUTPUT_FORMAT + "\n\n"
                + "本次主题：" + topic + "\n"
                + "老师本次输入：" + userMessage;
    }

    private String buildFollowUpPrompt(String userMessage, String latestLessonContent) {
        return "你是一名专业的幼儿园教案助手，请基于当前最新教案内容，按照老师的最新要求直接更新并输出完整教案。\n\n"
                + FIXED_OUTPUT_FORMAT + "\n\n"
                + "当前最新教案内容：\n" + latestLessonContent + "\n\n"
                + "老师最新补充要求：\n" + userMessage;
    }
    private String resolveGenerationMode(String latestLessonContent) {
        if (isBlank(latestLessonContent)) {
            return INITIAL_GENERATION_MODE;
        }
        return FOLLOW_UP_GENERATION_MODE;
    }

    private String getLatestLessonContent(ChatSessionDO session) {
        if (session.getCurrentResultId() == null) {
            return null;
        }
        LessonRecordDO latestRecord = lessonRecordMapper.selectById(session.getCurrentResultId());
        if (latestRecord == null || !latestRecord.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            return null;
        }
        if (!isBlank(latestRecord.getEditedContent())) {
            return latestRecord.getEditedContent();
        }
        return latestRecord.getResultContent();
    }

    private boolean isBlank(String content) {
        return content == null || content.isBlank();
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new BusinessException("输入数据转换失败");
        }
    }

    private LessonRecordRespVO toResp(LessonRecordDO record) {
        PromptTemplateDO template = promptTemplateService.getByIdAndUserId(record.getTemplateId(), record.getUserId());
        return LessonRecordRespVO.builder()
                .id(record.getId())
                .sessionId(record.getSessionId())
                .templateId(record.getTemplateId())
                .templateName(template == null ? null : template.getName())
                .llmConfigId(record.getLlmConfigId())
                .modelName(record.getModelName())
                .topic(record.getTopic())
                .inputPayload(record.getInputPayload())
                .finalPrompt(record.getFinalPrompt())
                .resultContent(record.getResultContent())
                .editedContent(record.getEditedContent())
                .feedbackRating(record.getFeedbackRating())
                .feedbackLevel(record.getFeedbackLevel())
                .feedbackComment(record.getFeedbackComment())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }

    private String normalizeComment(String comment) {
        if (comment == null) {
            return "";
        }
        return comment.trim();
    }
}
