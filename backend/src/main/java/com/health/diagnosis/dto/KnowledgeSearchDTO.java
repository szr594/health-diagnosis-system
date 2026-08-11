package com.health.diagnosis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 知识检索请求参数（转发给 AI 服务做向量检索）。
 */
@Data
public class KnowledgeSearchDTO {

    /** 检索问题 */
    @NotBlank(message = "检索关键词不能为空")
    private String query;

    /** 返回条数 */
    @Min(value = 1, message = "topK 最小为 1")
    @Max(value = 10, message = "topK 最大为 10")
    private Integer topK = 4;
}
