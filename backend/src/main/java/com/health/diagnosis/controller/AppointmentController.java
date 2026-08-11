package com.health.diagnosis.controller;

import com.health.diagnosis.common.PageResult;
import com.health.diagnosis.common.Result;
import com.health.diagnosis.common.UserContext;
import com.health.diagnosis.dto.AppointmentDTO;
import com.health.diagnosis.entity.Appointment;
import com.health.diagnosis.service.AppointmentService;
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
@RequestMapping("/api/appointment")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/book")
    @PreAuthorize("hasRole('PATIENT')")
    public Result<Appointment> book(@RequestBody @Valid AppointmentDTO dto) {
        Long patientId = UserContext.getUserId();
        return Result.success(appointmentService.book(dto, patientId));
    }

    @GetMapping("/list")
    public Result<PageResult<Appointment>> list(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        Integer role = UserContext.getRole();
        Long currentUserId = UserContext.getUserId();

        if (role != null && role == 0) {
            patientId = currentUserId;
        }
        return Result.success(appointmentService.pageList(patientId, doctorId, status, pageNum, pageSize));
    }

    @GetMapping("/detail/{id}")
    public Result<Appointment> detail(@PathVariable Long id) {
        return Result.success(appointmentService.detail(id));
    }

    @PostMapping("/confirm/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public Result<Appointment> confirm(@PathVariable Long id) {
        Long doctorUserId = UserContext.getUserId();
        return Result.success(appointmentService.confirm(id, doctorUserId));
    }

    @PostMapping("/complete/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public Result<Appointment> complete(@PathVariable Long id) {
        Long doctorUserId = UserContext.getUserId();
        return Result.success(appointmentService.complete(id, doctorUserId));
    }

    @PostMapping("/cancel/{id}")
    public Result<Appointment> cancel(@PathVariable Long id,
                                      @RequestParam(required = false) String reason) {
        Long operatorUserId = UserContext.getUserId();
        return Result.success(appointmentService.cancel(id, operatorUserId, reason));
    }
}
