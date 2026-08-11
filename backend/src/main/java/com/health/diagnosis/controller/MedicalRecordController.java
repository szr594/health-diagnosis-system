package com.health.diagnosis.controller;

import com.health.diagnosis.common.PageResult;
import com.health.diagnosis.common.Result;
import com.health.diagnosis.common.UserContext;
import com.health.diagnosis.dto.MedicalRecordDTO;
import com.health.diagnosis.entity.MedicalRecord;
import com.health.diagnosis.service.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/medical-record")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('DOCTOR')")
    public Result<MedicalRecord> create(@RequestBody @Valid MedicalRecordDTO dto) {
        Long doctorUserId = UserContext.getUserId();
        return Result.success(medicalRecordService.create(dto, doctorUserId));
    }

    @GetMapping("/list")
    public Result<PageResult<MedicalRecord>> list(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        Long currentUserId = UserContext.getUserId();
        Integer currentRole = UserContext.getRole();
        return Result.success(medicalRecordService.pageList(patientId, doctorId, currentUserId, currentRole, pageNum, pageSize));
    }

    @GetMapping("/detail/{id}")
    public Result<MedicalRecord> detail(@PathVariable Long id) {
        Long currentUserId = UserContext.getUserId();
        Integer currentRole = UserContext.getRole();
        return Result.success(medicalRecordService.detail(id, currentUserId, currentRole));
    }
}
