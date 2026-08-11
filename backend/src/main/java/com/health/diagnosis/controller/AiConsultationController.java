package com.health.diagnosis.controller;

import com.health.diagnosis.common.PageResult;
import com.health.diagnosis.common.Result;
import com.health.diagnosis.common.UserContext;
import com.health.diagnosis.dto.ConsultationRequest;
import com.health.diagnosis.dto.ConsultationVO;
import com.health.diagnosis.dto.HotQuestionVO;
import com.health.diagnosis.entity.ChatHistory;
import com.health.diagnosis.entity.DiagnosisSession;
import com.health.diagnosis.service.ChatHistoryService;
import com.health.diagnosis.service.ConsultationService;
import com.health.diagnosis.service.DiagnosisSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/medical")
@RequiredArgsConstructor
public class AiConsultationController {

    private final ConsultationService consultationService;
    private final DiagnosisSessionService sessionService;
    private final ChatHistoryService chatHistoryService;

    @PostMapping("/ai/pre-diagnosis")
    public Result<ConsultationVO> preDiagnosis(@RequestBody @Valid ConsultationRequest request) {
        Long userId = UserContext.getUserId();
        return Result.success(consultationService.preDiagnosis(request, userId));
    }

    @GetMapping("/consultation/list")
    public Result<PageResult<ConsultationVO>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        Long userId = UserContext.getUserId();
        return Result.success(consultationService.pageList(userId, pageNum, pageSize));
    }

    @GetMapping("/consultation/detail/{id}")
    public Result<ConsultationVO> detail(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        return Result.success(consultationService.detail(id, userId));
    }

    @DeleteMapping("/consultation/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        consultationService.delete(id, userId);
        return Result.success();
    }

    @GetMapping("/hot")
    public Result<List<HotQuestionVO>> hotQuestions() {
        return Result.success(consultationService.hotQuestions(10));
    }

    @GetMapping("/session/{sessionKey}/history")
    public Result<List<Map<String, Object>>> sessionHistory(@PathVariable String sessionKey) {
        Long userId = UserContext.getUserId();
        DiagnosisSession session = sessionService.findByKey(sessionKey);
        if (session == null || !session.getUserId().equals(userId)) {
            return Result.error(404, "会话不存在");
        }
        List<ChatHistory> histories = chatHistoryService.listBySession(session.getId());
        List<Map<String, Object>> result = histories.stream().map(h -> {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", h.getId());
            m.put("role", h.getRole());
            m.put("content", h.getContent());
            m.put("createTime", h.getCreateTime() != null ? h.getCreateTime().toString() : null);
            return m;
        }).collect(Collectors.toList());
        return Result.success(result);
    }
}
