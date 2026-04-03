package com.opensprout.lessonai.controller;

import com.opensprout.lessonai.common.model.ApiResponse;
import com.opensprout.lessonai.model.lesson.LessonGenerateReqVO;
import com.opensprout.lessonai.model.lesson.LessonRecordRespVO;
import com.opensprout.lessonai.model.lesson.LessonRecordUpdateReqVO;
import com.opensprout.lessonai.service.LessonRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lesson-records")
@RequiredArgsConstructor
public class LessonRecordController {

    private final LessonRecordService lessonRecordService;

    @PostMapping("/generate")
    public ApiResponse<LessonRecordRespVO> generate(@Valid @RequestBody LessonGenerateReqVO reqVO) {
        return ApiResponse.success(lessonRecordService.generate(reqVO));
    }

    @GetMapping("/session/{sessionId}")
    public ApiResponse<List<LessonRecordRespVO>> listBySessionId(@PathVariable("sessionId") Long sessionId) {
        return ApiResponse.success(lessonRecordService.listBySessionId(sessionId));
    }

    @PutMapping("/{id}")
    public ApiResponse<LessonRecordRespVO> update(@PathVariable("id") Long id,
                                                  @Valid @RequestBody LessonRecordUpdateReqVO reqVO) {
        return ApiResponse.success(lessonRecordService.update(id, reqVO));
    }

}
