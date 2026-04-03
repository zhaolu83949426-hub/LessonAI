package com.opensprout.lessonai.service;

import com.opensprout.lessonai.domain.entity.ChatSessionDO;
import com.opensprout.lessonai.model.session.ChatSessionCreateReqVO;
import com.opensprout.lessonai.model.session.ChatSessionRespVO;
import com.opensprout.lessonai.model.session.SessionMessageRespVO;

import java.util.List;

public interface ChatSessionService {

    Long create(ChatSessionCreateReqVO reqVO);

    List<ChatSessionRespVO> list();

    ChatSessionRespVO get(Long id);

    List<SessionMessageRespVO> listMessages(Long sessionId);

    void delete(Long id);

    ChatSessionDO getByIdAndUserId(Long id, Long userId);

    void updateCurrentResult(Long sessionId, Long lessonRecordId);

}
