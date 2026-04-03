package com.opensprout.lessonai.controller;

import com.opensprout.lessonai.common.model.ApiResponse;
import com.opensprout.lessonai.model.session.ChatSessionCreateReqVO;
import com.opensprout.lessonai.model.session.ChatSessionRespVO;
import com.opensprout.lessonai.model.session.SessionMessageRespVO;
import com.opensprout.lessonai.service.ChatSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class ChatSessionController {

    private final ChatSessionService chatSessionService;

    @GetMapping
    public ApiResponse<List<ChatSessionRespVO>> list() {
        return ApiResponse.success(chatSessionService.list());
    }

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody ChatSessionCreateReqVO reqVO) {
        return ApiResponse.success(chatSessionService.create(reqVO));
    }

    @GetMapping("/{id}")
    public ApiResponse<ChatSessionRespVO> get(@PathVariable Long id) {
        return ApiResponse.success(chatSessionService.get(id));
    }

    @GetMapping("/{id}/messages")
    public ApiResponse<List<SessionMessageRespVO>> listMessages(@PathVariable Long id) {
        return ApiResponse.success(chatSessionService.listMessages(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        chatSessionService.delete(id);
        return ApiResponse.success();
    }

}
