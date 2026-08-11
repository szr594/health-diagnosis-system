package com.health.diagnosis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 问诊记录实体，对应表 consultation_record。
 */
@Data
@TableName("consultation_record")
public class ConsultationRecord implements Serializable {

    /** 主键 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 患者用户 ID */
    private Long userId;

    /** 关联会话 ID */
    private Long sessionId;

    /** 症状描述 */
    private String symptomDescription;

    /** 症状持续时间 */
    private String symptomDuration;

    /** 主诉（精简） */
    private String chiefComplaint;

    /** AI 问诊建议（完整文本） */
    private String aiAdvice;

    /** 结构化建议（JSON） */
    private String structuredAdvice;

    /** 风险等级：low / mid / high */
    private String riskLevel;

    /** 疑似疾病（逗号分隔） */
    private String possibleDiseases;

    /** 建议就诊科室 */
    private String suggestedDepartment;

    /** 状态：0处理中 1完成 2失败 */
    private Integer status;

    /** 失败原因（降级说明） */
    private String failReason;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
