package com.health.diagnosis.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.health.diagnosis.common.BizException;
import com.health.diagnosis.common.PageResult;
import com.health.diagnosis.dto.KnowledgeDTO;
import com.health.diagnosis.dto.KnowledgeSearchDTO;
import com.health.diagnosis.dto.KnowledgeSearchResult;
import com.health.diagnosis.entity.HealthKnowledge;
import com.health.diagnosis.mapper.HealthKnowledgeMapper;
import com.health.diagnosis.service.HealthKnowledgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthKnowledgeServiceImpl implements HealthKnowledgeService {

    private final HealthKnowledgeMapper knowledgeMapper;
    private final RestTemplate restTemplate;

    @Value("${health.ai.base-url}")
    private String aiBaseUrl;

    @Override
    public HealthKnowledge create(KnowledgeDTO dto) {
        HealthKnowledge entity = new HealthKnowledge();
        entity.setTitle(dto.getTitle());
        entity.setCategory(dto.getCategory());
        entity.setContent(dto.getContent());
        entity.setSource(dto.getSource());
        entity.setStatus(1);
        knowledgeMapper.insert(entity);
        try {
            callAiVectorize(entity);
        } catch (Exception e) {
            log.warn("知识文档向量化失败，稍后需手动同步: id={}, err={}", entity.getId(), e.getMessage());
        }
        log.info("新增知识文档: id={}, title={}", entity.getId(), entity.getTitle());
        return entity;
    }

    @Override
    public HealthKnowledge update(Long id, KnowledgeDTO dto) {
        HealthKnowledge entity = knowledgeMapper.selectById(id);
        if (entity == null) {
            throw new BizException(404, "知识文档不存在");
        }
        entity.setTitle(dto.getTitle());
        entity.setCategory(dto.getCategory());
        entity.setContent(dto.getContent());
        entity.setSource(dto.getSource());
        knowledgeMapper.updateById(entity);
        try {
            callAiVectorize(entity);
        } catch (Exception e) {
            log.warn("知识文档重新向量化失败: id={}, err={}", id, e.getMessage());
        }
        log.info("更新知识文档: id={}", id);
        return entity;
    }

    @Override
    public void delete(Long id) {
        HealthKnowledge entity = knowledgeMapper.selectById(id);
        if (entity == null) {
            throw new BizException(404, "知识文档不存在");
        }
        knowledgeMapper.deleteById(id);
        try {
            callAiDelete(id);
        } catch (Exception e) {
            log.warn("向量删除失败: id={}, err={}", id, e.getMessage());
        }
        log.info("删除知识文档: id={}", id);
    }

    @Override
    public PageResult<HealthKnowledge> pageList(String keyword, long pageNum, long pageSize) {
        IPage<HealthKnowledge> page = knowledgeMapper.selectPage(
                new Page<>(pageNum, pageSize),
                Wrappers.<HealthKnowledge>lambdaQuery()
                        .and(StrUtil.isNotBlank(keyword), wrapper -> wrapper
                                .like(HealthKnowledge::getTitle, keyword)
                                .or()
                                .like(HealthKnowledge::getContent, keyword))
                        .orderByDesc(HealthKnowledge::getCreateTime));
        return PageResult.of(page);
    }

    @Override
    public HealthKnowledge detail(Long id) {
        HealthKnowledge entity = knowledgeMapper.selectById(id);
        if (entity == null) {
            throw new BizException(404, "知识文档不存在");
        }
        return entity;
    }

    @Override
    public List<KnowledgeSearchResult.Item> search(KnowledgeSearchDTO dto) {
        String url = aiBaseUrl + "/api/ai/knowledge/search";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("query", dto.getQuery());
        body.put("top_k", dto.getTopK() == null ? 4 : dto.getTopK());

        try {
            ResponseEntity<KnowledgeSearchResult> response = restTemplate.postForEntity(
                    url, new HttpEntity<>(body, headers), KnowledgeSearchResult.class);
            KnowledgeSearchResult result = response.getBody();
            return result == null || result.getData() == null
                    ? Collections.emptyList() : result.getData();
        } catch (Exception e) {
            log.error("知识检索失败", e);
            throw new BizException(500, "知识检索服务暂不可用");
        }
    }

    private void callAiVectorize(HealthKnowledge entity) {
        String url = aiBaseUrl + "/api/ai/knowledge/upload";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("id", entity.getId());
        body.put("title", entity.getTitle());
        body.put("category", entity.getCategory() == null ? "" : entity.getCategory());
        body.put("content", entity.getContent());

        restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
    }

    private void callAiDelete(Long id) {
        String url = aiBaseUrl + "/api/ai/knowledge/delete";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("id", id);

        restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
    }
}
