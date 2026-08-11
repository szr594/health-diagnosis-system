package com.health.diagnosis.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 问诊记录展示对象：整合 AI 结果与患者信息。
 */
@Data
public class ConsultationVO {

    /** 记录 ID */
    private Long id;

    /** 患者用户 ID */
    private Long userId;

    /** 关联会话 ID */
    private Long sessionId;

    /** 会话 Key */
    private String sessionKey;

    /** 患者昵称 */
    private String nickname;

    /** 症状描述 */
    private String symptomDescription;

    /** 症状持续时间 */
    private String symptomDuration;

    /** AI 问诊建议（完整文本） */
    private String aiAdvice;

    /** 结构化建议（JSON 字符串） */
    private String structuredAdvice;

    /** 风险等级：low / mid / high */
    private String riskLevel;

    /** 疑似疾病（逗号分隔） */
    private String possibleDiseases;

    /** 建议就诊科室 */
    private String suggestedDepartment;

    /** 状态：0处理中 1完成 2失败 */
    private Integer status;

    /** 失败原因 */
    private String failReason;

    /** 创建时间 */
    private LocalDateTime createTime;
}
