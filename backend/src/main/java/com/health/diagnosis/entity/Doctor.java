package com.health.diagnosis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 医生实体，对应表 t_doctor。
 *
 * <p>通过 user_id 关联 t_user（角色=1医生），通过 department_id 关联 t_department。
 * 查询时可通过 {@link #departmentName} 和 {@link #doctorName} 回填关联名称。</p>
 */
@Data
@TableName("t_doctor")
public class Doctor implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联用户ID（t_user.id） */
    private Long userId;

    /** 所属科室ID */
    private Long departmentId;

    /** 职称 */
    private String title;

    /** 擅长领域 */
    private String specialty;

    /** 医生简介 */
    private String description;

    /** 挂号费（元） */
    private BigDecimal consultationFee;

    /** 每日最大接诊量 */
    private Integer maxDailyAppointments;

    /** 状态：0停诊 1接诊 */
    private Integer status;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // ===== 非表字段（联表查询回填） =====

    /** 医生真实姓名（关联 t_user.real_name） */
    @TableField(exist = false)
    private String doctorName;

    /** 科室名称（关联 t_department.name） */
    @TableField(exist = false)
    private String departmentName;

    /** 医生头像（关联 t_user.avatar） */
    @TableField(exist = false)
    private String avatar;
}
