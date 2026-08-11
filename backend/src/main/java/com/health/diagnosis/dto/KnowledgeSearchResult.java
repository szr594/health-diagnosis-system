package com.health.diagnosis.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Python AI 服务知识检索响应模型。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KnowledgeSearchResult {

    /** 状态码 */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 检索结果列表 */
    private List<Item> data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {

        /** 文档 ID */
        @JsonProperty("doc_id")
        private Integer docId;

        /** 文档标题 */
        private String title;

        /** 分类 */
        private String category;

        /** 命中片段 */
        private String content;

        /** 相似度得分 */
        private Double score;
    }
}
