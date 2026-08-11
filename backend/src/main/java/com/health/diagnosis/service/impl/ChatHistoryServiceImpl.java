package com.health.diagnosis.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.health.diagnosis.entity.ChatHistory;
import com.health.diagnosis.mapper.ChatHistoryMapper;
import com.health.diagnosis.service.ChatHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 问诊对话历史服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatHistoryServiceImpl implements ChatHistoryService {

    private final ChatHistoryMapper chatHistoryMapper;

    @Override
    public void save(Long userId, Long consultationId, String role, String content) {
        ChatHistory history = new ChatHistory();
        history.setUserId(userId);
        history.setConsultationId(consultationId);
        history.setRole(role);
        history.setContent(content);
        chatHistoryMapper.insert(history);
    }

    @Override
    public void saveWithSession(Long userId, Long consultationId, Long sessionId, String role, String content) {
        ChatHistory history = new ChatHistory();
        history.setUserId(userId);
        history.setConsultationId(consultationId);
        history.setSessionId(sessionId);
        history.setRole(role);
        history.setContent(content);
        chatHistoryMapper.insert(history);
    }

    @Override
    public List<ChatHistory> listByConsultation(Long consultationId) {
        return chatHistoryMapper.selectList(
                Wrappers.<ChatHistory>lambdaQuery()
                        .eq(ChatHistory::getConsultationId, consultationId)
                        .orderByAsc(ChatHistory::getCreateTime));
    }

    @Override
    public List<ChatHistory> listBySession(Long sessionId) {
        return chatHistoryMapper.selectList(
                Wrappers.<ChatHistory>lambdaQuery()
                        .eq(ChatHistory::getSessionId, sessionId)
                        .orderByAsc(ChatHistory::getCreateTime));
    }

    @Override
    public List<ChatHistory> listRecentBySession(Long sessionId, int limit) {
        // 先查总数，再偏移取最后 N 条
        long total = chatHistoryMapper.selectCount(
                Wrappers.<ChatHistory>lambdaQuery()
                        .eq(ChatHistory::getSessionId, sessionId));
        long offset = Math.max(0, total - limit);
        return chatHistoryMapper.selectList(
                Wrappers.<ChatHistory>lambdaQuery()
                        .eq(ChatHistory::getSessionId, sessionId)
                        .orderByAsc(ChatHistory::getCreateTime)
                        .last("LIMIT " + offset + "," + limit));
    }

    @Override
    public String buildHistoryText(Long sessionId, int maxMessages) {
        if (sessionId == null) return "";

        List<ChatHistory> recent = listRecentBySession(sessionId, maxMessages);
        if (recent.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("【对话历史】\n");
        for (int i = 0; i < recent.size(); i++) {
            ChatHistory msg = recent.get(i);
            String tag = "user".equals(msg.getRole()) ? "[第" + ((i / 2) + 1) + "轮 - 用户]" : "[第" + ((i / 2) + 1) + "轮 - AI]";
            sb.append(tag).append(" ").append(msg.getContent()).append("\n");
        }
        return sb.toString();
    }
}
