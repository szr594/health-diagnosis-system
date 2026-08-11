package com.health.diagnosis.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 医生信息 DTO（管理员新增/更新）。
 */
@Data
public class DoctorDTO {

    @NotBlank(message = "用户ID不能为空")
    private String userId;

    private Long departmentId;
    private String title;
    private String specialty;
    private String description;
    private BigDecimal consultationFee;
    private Integer maxDailyAppointments;
    private Integer status;
}
