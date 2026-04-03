package com.opensprout.lessonai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.opensprout.lessonai.common.exception.BusinessException;
import com.opensprout.lessonai.domain.entity.ChatSessionDO;
import com.opensprout.lessonai.domain.entity.PromptTemplateDO;
import com.opensprout.lessonai.domain.entity.SessionMessageDO;
import com.opensprout.lessonai.mapper.ChatSessionMapper;
import com.opensprout.lessonai.mapper.SessionMessageMapper;
import com.opensprout.lessonai.model.session.ChatSessionCreateReqVO;
import com.opensprout.lessonai.model.session.ChatSessionRespVO;
import com.opensprout.lessonai.model.session.SessionMessageRespVO;
import com.opensprout.lessonai.security.SecurityUtils;
import com.opensprout.lessonai.service.ChatSessionService;
import com.opensprout.lessonai.service.LlmConfigService;
import com.opensprout.lessonai.service.PromptTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl implements ChatSessionService {

    private final ChatSessionMapper chatSessionMapper;
    private final SessionMessageMapper sessionMessageMapper;
    private final PromptTemplateService promptTemplateService;
    private final LlmConfigService llmConfigService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ChatSessionCreateReqVO reqVO) {
        Long userId = SecurityUtils.getCurrentUserId();
        PromptTemplateDO template = promptTemplateService.getByIdAndUserId(reqVO.getTemplateId(), userId);
        if (template == null) {
            throw new BusinessException("模板不存在");
        }
        llmConfigService.getEnabledById(reqVO.getLlmConfigId());
        ChatSessionDO session = new ChatSessionDO();
        session.setUserId(userId);
        session.setTitle(reqVO.getTitle().trim());
        session.setTemplateId(reqVO.getTemplateId());
        session.setLlmConfigId(reqVO.getLlmConfigId());
        chatSessionMapper.insert(session);
        return session.getId();
    }

    @Override
    public List<ChatSessionRespVO> list() {
        Long userId = SecurityUtils.getCurrentUserId();
        return chatSessionMapper.selectList(new LambdaQueryWrapper<ChatSessionDO>()
                        .eq(ChatSessionDO::getUserId, userId)
                        .orderByDesc(ChatSessionDO::getUpdatedAt))
                .stream()
                .map(this::toResp)
                .toList();
    }

    @Override
    public ChatSessionRespVO get(Long id) {
        return toResp(requireOwnedSession(id));
    }

    @Override
    public List<SessionMessageRespVO> listMessages(Long sessionId) {
        ChatSessionDO session = requireOwnedSession(sessionId);
        return sessionMessageMapper.selectList(new LambdaQueryWrapper<SessionMessageDO>()
                        .eq(SessionMessageDO::getSessionId, session.getId())
                        .orderByAsc(SessionMessageDO::getCreatedAt)
                        .orderByAsc(SessionMessageDO::getId))
                .stream()
                .map(message -> SessionMessageRespVO.builder()
                        .id(message.getId())
                        .role(message.getRole())
                        .content(message.getContent())
                        .lessonRecordId(message.getLessonRecordId())
                        .createdAt(message.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ChatSessionDO session = requireOwnedSession(id);
        chatSessionMapper.deleteById(session.getId());
    }

    @Override
    public ChatSessionDO getByIdAndUserId(Long id, Long userId) {
        return chatSessionMapper.selectOne(new LambdaQueryWrapper<ChatSessionDO>()
                .eq(ChatSessionDO::getId, id)
                .eq(ChatSessionDO::getUserId, userId)
                .last("LIMIT 1"));
    }

    @Override
    public void updateCurrentResult(Long sessionId, Long lessonRecordId) {
        ChatSessionDO session = requireOwnedSession(sessionId);
        session.setCurrentResultId(lessonRecordId);
        chatSessionMapper.updateById(session);
    }

    private ChatSessionDO requireOwnedSession(Long id) {
        ChatSessionDO session = getByIdAndUserId(id, SecurityUtils.getCurrentUserId());
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        return session;
    }

    private ChatSessionRespVO toResp(ChatSessionDO session) {
        SessionMessageDO latestQuestion = sessionMessageMapper.selectOne(new LambdaQueryWrapper<SessionMessageDO>()
                .eq(SessionMessageDO::getSessionId, session.getId())
                .eq(SessionMessageDO::getRole, "USER")
                .orderByDesc(SessionMessageDO::getId)
                .last("LIMIT 1"));
        return ChatSessionRespVO.builder()
                .id(session.getId())
                .title(session.getTitle())
                .templateId(session.getTemplateId())
                .llmConfigId(session.getLlmConfigId())
                .currentResultId(session.getCurrentResultId())
                .latestQuestion(latestQuestion == null ? null : latestQuestion.getContent())
                .resultStatus(session.getCurrentResultId() == null ? "DRAFT" : "GENERATED")
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }

}
