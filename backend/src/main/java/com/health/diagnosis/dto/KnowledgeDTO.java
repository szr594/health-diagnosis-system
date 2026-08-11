package com.health.diagnosis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 健康知识文档新增/更新请求参数。
 */
@Data
public class KnowledgeDTO {

    /** 知识文档标题 */
    @NotBlank(message = "文档标题不能为空")
    @Size(max = 200, message = "文档标题不能超过 200 字")
    private String title;

    /** 分类 */
    @Size(max = 50, message = "分类不能超过 50 字")
    private String category;

    /** 知识内容（原文） */
    @NotBlank(message = "文档内容不能为空")
    private String content;

    /** 来源 */
    @Size(max = 200, message = "来源不能超过 200 字")
    private String source;
}
