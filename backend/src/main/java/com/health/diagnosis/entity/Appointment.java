package com.health.diagnosis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约挂号实体，对应表 t_appointment。
 */
@Data
@TableName("t_appointment")
public class Appointment implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 预约编号（唯一） */
    private String appointmentNo;

    /** 患者用户ID */
    private Long patientId;

    /** 医生ID */
    private Long doctorId;

    /** 科室ID */
    private Long departmentId;

    /** 预约就诊日期 */
    private LocalDate appointmentDate;

    /** 时段：上午/下午/晚间 */
    private String timeSlot;

    /** 就诊原因/症状简述 */
    private String reason;

    /** 状态：0待确认 1已确认 2已完成 3已取消 */
    private Integer status;

    /** 取消原因 */
    private String cancelReason;

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
