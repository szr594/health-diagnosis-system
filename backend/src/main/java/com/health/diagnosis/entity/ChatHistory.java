package com.health.diagnosis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 问诊对话历史实体，对应表 chat_history。
 */
@Data
@TableName("chat_history")
public class ChatHistory implements Serializable {

    /** 主键 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 关联问诊记录 ID */
    private Long consultationId;

    /** 关联会话 ID（多轮对话场景） */
    private Long sessionId;

    /** 角色：user 用户 / assistant AI */
    private String role;

    /** 对话内容 */
    private String content;

    /** 创建时间 */
    private LocalDateTime createTime;
}
