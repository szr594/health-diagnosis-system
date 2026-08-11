package com.health.diagnosis.service;

import com.health.diagnosis.entity.DiagnosisSession;

/**
 * 问诊会话服务接口：管理多轮对话会话的生命周期。
 */
public interface DiagnosisSessionService {

    /**
     * 创建新会话（或根据 sessionKey 查找已有会话）。
     *
     * @param sessionKey 前端传的会话唯一标识，为空则自动生成
     * @param userId     用户 ID
     * @return 会话实体（新建或已有）
     */
    DiagnosisSession getOrCreate(String sessionKey, Long userId);

    /**
     * 根据 sessionKey 查询会话。
     */
    DiagnosisSession findByKey(String sessionKey);

    /**
     * 更新会话摘要（AI 对历史对话的压缩）。
     */
    void updateSummary(Long sessionId, String summary);

    /**
     * 递增消息计数。
     */
    void incrementMessageCount(Long sessionId, int delta);

    /**
     * 标记会话完成。
     */
    void completeSession(Long sessionId);
}
