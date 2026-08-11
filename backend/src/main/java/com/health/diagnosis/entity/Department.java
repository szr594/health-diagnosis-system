package com.health.diagnosis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 科室实体，对应表 t_department。
 */
@Data
@TableName("t_department")
public class Department implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 科室名称 */
    private String name;

    /** 科室编码 */
    private String code;

    /** 科室简介 */
    private String description;

    /** 科室位置 */
    private String location;

    /** 排序序号 */
    private Integer sortOrder;

    /** 状态：0停用 1启用 */
    private Integer status;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
