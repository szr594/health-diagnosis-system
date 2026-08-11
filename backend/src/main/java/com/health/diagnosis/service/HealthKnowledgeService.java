package com.health.diagnosis.service;

import com.health.diagnosis.common.PageResult;
import com.health.diagnosis.dto.KnowledgeDTO;
import com.health.diagnosis.dto.KnowledgeSearchDTO;
import com.health.diagnosis.dto.KnowledgeSearchResult;
import com.health.diagnosis.entity.HealthKnowledge;

import java.util.List;

/**
 * 健康知识库服务接口。
 */
public interface HealthKnowledgeService {

    /**
     * 新增知识文档（同步向量化）。
     */
    HealthKnowledge create(KnowledgeDTO dto);

    /**
     * 更新知识文档（重新向量化）。
     */
    HealthKnowledge update(Long id, KnowledgeDTO dto);

    /**
     * 删除知识文档（同步删除向量）。
     */
    void delete(Long id);

    /**
     * 分页查询知识文档，支持关键词过滤。
     */
    PageResult<HealthKnowledge> pageList(String keyword, long pageNum, long pageSize);

    /**
     * 查询文档详情。
     */
    HealthKnowledge detail(Long id);

    /**
     * 向量检索（转发 AI 服务）。
     */
    List<KnowledgeSearchResult.Item> search(KnowledgeSearchDTO dto);
}
