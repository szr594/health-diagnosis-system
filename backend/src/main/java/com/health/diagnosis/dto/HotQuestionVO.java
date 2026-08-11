package com.health.diagnosis.dto;

import lombok.Data;

/**
 * 热门问诊问题（基于 Redis ZSet 排行）。
 */
@Data
public class HotQuestionVO {

    /** 症状关键词 */
    private String symptom;

    /** 问询次数 */
    private Double count;
}
