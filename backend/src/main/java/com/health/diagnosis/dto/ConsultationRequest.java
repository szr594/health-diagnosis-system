package com.health.diagnosis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI 预问诊请求参数。
 */
@Data
public class ConsultationRequest {

    /** 会话唯一标识（首次为空，后端自动创建新会话） */
    private String sessionId;

    /** 症状描述（必填） */
    @NotBlank(message = "症状描述不能为空")
    @Size(max = 2000, message = "症状描述不能超过 2000 字")
    private String symptomDescription;

    /** 症状持续时间 */
    @Size(max = 50, message = "症状持续时间不能超过 50 字")
    private String symptomDuration;

    /** 年龄 */
    private Integer age;

    /** 性别：male / female / unknown */
    private String gender;

    /** 既往病史 */
    @Size(max = 500, message = "既往病史不能超过 500 字")
    private String medicalHistory;

    /** 过敏史 */
    @Size(max = 500, message = "过敏史不能超过 500 字")
    private String allergyHistory;
}
