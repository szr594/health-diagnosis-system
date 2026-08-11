package com.health.diagnosis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 问诊会话实体，对应表 diagnosis_session。
 *
 * <p>一次完整的问诊会话包含多轮 user-assistant 对话，
 * session_key 是前端持有的会话唯一标识（UUID），
 * summary 是 AI 对历史对话的压缩摘要，用于控制 token 预算。</p>
 */
@Data
@TableName("diagnosis_session")
public class DiagnosisSession implements Serializable {

    /** 主键 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话唯一标识（UUID v4） */
    private String sessionKey;

    /** 用户 ID */
    private Long userId;

    /** 状态：0进行中 1已完成 2已归档 */
    private Integer status;

    /** AI 生成的会话摘要（压缩态） */
    private String summary;

    /** 消息总数 */
    private Integer messageCount;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
