package com.health.diagnosis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 健康知识文档实体，对应表 health_knowledge。
 *
 * <p>MySQL 保存原文，向量索引由 AI 服务同步维护在 ChromaDB。</p>
 */
@Data
@TableName("health_knowledge")
public class HealthKnowledge implements Serializable {

    /** 主键 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 知识文档标题 */
    private String title;

    /** 分类（内科/心血管/呼吸/消化等） */
    private String category;

    /** 知识内容（原文） */
    private String content;

    /** 来源 */
    private String source;

    /** 状态：0下架 1上架 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
