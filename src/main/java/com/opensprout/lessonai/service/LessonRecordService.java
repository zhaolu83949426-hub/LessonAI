package com.opensprout.lessonai.service;

import com.opensprout.lessonai.model.lesson.LessonGenerateReqVO;
import com.opensprout.lessonai.model.lesson.LessonRecordRespVO;
import com.opensprout.lessonai.model.lesson.LessonRecordUpdateReqVO;

import java.util.List;

public interface LessonRecordService {

    LessonRecordRespVO generate(LessonGenerateReqVO reqVO);

    List<LessonRecordRespVO> listBySessionId(Long sessionId);

    LessonRecordRespVO update(Long id, LessonRecordUpdateReqVO reqVO);

}
