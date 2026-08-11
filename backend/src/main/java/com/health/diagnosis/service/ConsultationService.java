package com.health.diagnosis.service;

import com.health.diagnosis.common.PageResult;
import com.health.diagnosis.dto.ConsultationRequest;
import com.health.diagnosis.dto.ConsultationVO;
import com.health.diagnosis.dto.HotQuestionVO;

import java.util.List;

/**
 * AI 预问诊服务接口。
 */
public interface ConsultationService {

    /**
     * AI 预问诊：异步调用 Python 服务，返回结构化问诊建议。
     */
    ConsultationVO preDiagnosis(ConsultationRequest request, Long userId);

    /**
     * 分页查询当前用户的问诊记录。
     */
    PageResult<ConsultationVO> pageList(Long userId, long pageNum, long pageSize);

    /**
     * 查询问诊记录详情（校验归属）。
     */
    ConsultationVO detail(Long id, Long userId);

    /**
     * 删除问诊记录（校验归属）。
     */
    void delete(Long id, Long userId);

    /**
     * 热门问诊问题排行（Redis ZSet）。
     */
    List<HotQuestionVO> hotQuestions(int topN);
}
