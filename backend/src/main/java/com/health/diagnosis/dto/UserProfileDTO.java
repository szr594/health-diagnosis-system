package com.health.diagnosis.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户个人资料更新请求。
 */
@Data
public class UserProfileDTO {

    private String nickname;

    private String realName;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 0未知 1男 2女 */
    private Integer gender;

    private Integer age;

    private BigDecimal height;

    private BigDecimal weight;

    private String allergyHistory;

    private String medicalHistory;
}
