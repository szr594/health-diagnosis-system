package com.health.diagnosis.service;

import com.health.diagnosis.entity.ChatHistory;

import java.util.List;

/**
 * 问诊对话历史服务接口。
 */
public interface ChatHistoryService {

    /**
     * 保存一条对话消息。
     */
    void save(Long userId, Long consultationId, String role, String content);

    /**
     * 保存一条对话消息（含 sessionId）。
     */
    void saveWithSession(Long userId, Long consultationId, Long sessionId, String role, String content);

    /**
     * 查询某次问诊的全部对话。
     */
    List<ChatHistory> listByConsultation(Long consultationId);

    /**
     * 查询某次会话的全部对话（按时间升序）。
     */
    List<ChatHistory> listBySession(Long sessionId);

    /**
     * 查询某次会话的最近 N 条对话（滑动窗口）。
     */
    List<ChatHistory> listRecentBySession(Long sessionId, int limit);

    /**
     * 将对话历史格式化为 prompt 可用的文本。
     *
     * @param sessionId   会话 ID
     * @param maxMessages 最多取最近 N 条消息
     * @return 格式化的对话历史文本，如 "[用户]: xxx\n[AI]: xxx"
     */
    String buildHistoryText(Long sessionId, int maxMessages);
}
