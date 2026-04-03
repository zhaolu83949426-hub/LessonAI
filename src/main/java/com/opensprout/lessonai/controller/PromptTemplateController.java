package com.opensprout.lessonai.controller;

import com.opensprout.lessonai.common.model.ApiResponse;
import com.opensprout.lessonai.model.template.PromptTemplateRespVO;
import com.opensprout.lessonai.model.template.PromptTemplateSaveReqVO;
import com.opensprout.lessonai.service.PromptTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class PromptTemplateController {

    private final PromptTemplateService promptTemplateService;

    @GetMapping
    public ApiResponse<List<PromptTemplateRespVO>> list() {
        return ApiResponse.success(promptTemplateService.list());
    }

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody PromptTemplateSaveReqVO reqVO) {
        return ApiResponse.success(promptTemplateService.create(reqVO));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable("id") Long id,
                                    @Valid @RequestBody PromptTemplateSaveReqVO reqVO) {
        promptTemplateService.update(id, reqVO);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        promptTemplateService.delete(id);
        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    public ApiResponse<PromptTemplateRespVO> get(@PathVariable("id") Long id) {
        return ApiResponse.success(promptTemplateService.get(id));
    }

}
