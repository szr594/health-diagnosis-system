package com.health.diagnosis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 电子病历实体，对应表 t_medical_record。
 */
@Data
@TableName("t_medical_record")
public class MedicalRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 病历编号（唯一） */
    private String recordNo;

    /** 患者用户ID */
    private Long patientId;

    /** 医生ID */
    private Long doctorId;

    /** 科室ID */
    private Long departmentId;

    /** 关联预约ID */
    private Long appointmentId;

    /** 主诉 */
    private String chiefComplaint;

    /** 现病史 */
    private String presentIllness;

    /** 既往史 */
    private String pastHistory;

    /** 体格检查 */
    private String physicalExam;

    /** 诊断结果 */
    private String diagnosis;

    /** 治疗方案 */
    private String treatmentPlan;

    /** 处方信息 */
    private String prescription;

    /** 备注 */
    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // ===== 非表字段（联表查询回填） =====

    /** 患者姓名 */
    @TableField(exist = false)
    private String patientName;

    /** 医生姓名 */
    @TableField(exist = false)
    private String doctorName;

    /** 科室名称 */
    @TableField(exist = false)
    private String departmentName;
}
