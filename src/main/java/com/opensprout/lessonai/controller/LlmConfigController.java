package com.opensprout.lessonai.controller;

import com.opensprout.lessonai.common.model.ApiResponse;
import com.opensprout.lessonai.model.llm.LlmConfigOptionRespVO;
import com.opensprout.lessonai.service.LlmConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/llm-configs")
@RequiredArgsConstructor
public class LlmConfigController {

    private final LlmConfigService llmConfigService;

    @GetMapping("/options")
    public ApiResponse<List<LlmConfigOptionRespVO>> options() {
        return ApiResponse.success(llmConfigService.listOptions());
    }

    @GetMapping("/default")
    public ApiResponse<LlmConfigOptionRespVO> defaultOption() {
        return ApiResponse.success(llmConfigService.getDefaultOption());
    }

}
