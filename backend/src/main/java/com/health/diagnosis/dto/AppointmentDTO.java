package com.health.diagnosis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 预约挂号请求 DTO。
 */
@Data
public class AppointmentDTO {

    @NotNull(message = "医生ID不能为空")
    private Long doctorId;

    @NotNull(message = "科室ID不能为空")
    private Long departmentId;

    @NotNull(message = "预约日期不能为空")
    private LocalDate appointmentDate;

    @NotBlank(message = "时段不能为空")
    private String timeSlot;

    /** 就诊原因 */
    private String reason;

    /** 备注 */
    private String remark;
}
