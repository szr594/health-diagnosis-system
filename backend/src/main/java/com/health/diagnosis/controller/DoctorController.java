package com.health.diagnosis.controller;

import com.health.diagnosis.common.PageResult;
import com.health.diagnosis.common.Result;
import com.health.diagnosis.common.UserContext;
import com.health.diagnosis.dto.DoctorDTO;
import com.health.diagnosis.entity.Doctor;
import com.health.diagnosis.service.DoctorService;
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

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping("/list")
    public Result<PageResult<Doctor>> list(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(doctorService.pageList(departmentId, keyword, pageNum, pageSize));
    }

    @GetMapping("/detail/{id}")
    public Result<Doctor> detail(@PathVariable Long id) {
        return Result.success(doctorService.detail(id));
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Doctor> create(@RequestBody @Valid DoctorDTO dto) {
        return Result.success(doctorService.create(dto));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Doctor> update(@PathVariable Long id, @RequestBody @Valid DoctorDTO dto) {
        return Result.success(doctorService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        doctorService.delete(id);
        return Result.success();
    }

    @GetMapping("/my-profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public Result<Doctor> myProfile() {
        Long userId = UserContext.getUserId();
        return Result.success(doctorService.getByUserId(userId));
    }
}
