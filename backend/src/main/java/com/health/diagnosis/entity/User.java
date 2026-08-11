package com.health.diagnosis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户/患者实体，对应表 t_user。
 */
@Data
@TableName("t_user")
public class User implements Serializable {

    /** 主键 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录用户名 */
    private String username;

    /** 登录密码（BCrypt 加密），序列化时忽略避免泄露 */
    @JsonIgnore
    private String password;

    /** 昵称 */
    private String nickname;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 性别：0未知 1男 2女 */
    private Integer gender;

    /** 年龄 */
    private Integer age;

    /** 身高(cm) */
    private BigDecimal height;

    /** 体重(kg) */
    private BigDecimal weight;

    /** 过敏史 */
    private String allergyHistory;

    /** 既往病史 */
    private String medicalHistory;

    /** 角色：0患者 1医生 2管理员 */
    private Integer role;

    /** 头像 URL */
    private String avatar;

    /** 状态：0禁用 1启用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 登录返回的令牌（非表字段） */
    @TableField(exist = false)
    private String token;
}
