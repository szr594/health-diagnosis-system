package com.health.diagnosis.controller;

import com.health.diagnosis.common.PageResult;
import com.health.diagnosis.common.Result;
import com.health.diagnosis.dto.KnowledgeDTO;
import com.health.diagnosis.dto.KnowledgeSearchDTO;
import com.health.diagnosis.dto.KnowledgeSearchResult;
import com.health.diagnosis.entity.HealthKnowledge;
import com.health.diagnosis.service.HealthKnowledgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final HealthKnowledgeService knowledgeService;

    @GetMapping("/list")
    public Result<PageResult<HealthKnowledge>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(knowledgeService.pageList(keyword, pageNum, pageSize));
    }

    @GetMapping("/detail/{id}")
    public Result<HealthKnowledge> detail(@PathVariable Long id) {
        return Result.success(knowledgeService.detail(id));
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<HealthKnowledge> create(@RequestBody @Valid KnowledgeDTO dto) {
        return Result.success(knowledgeService.create(dto));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<HealthKnowledge> update(@PathVariable Long id, @RequestBody @Valid KnowledgeDTO dto) {
        return Result.success(knowledgeService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        knowledgeService.delete(id);
        return Result.success();
    }

    @PostMapping("/search")
    public Result<List<KnowledgeSearchResult.Item>> search(@RequestBody @Valid KnowledgeSearchDTO dto) {
        return Result.success(knowledgeService.search(dto));
    }
}
