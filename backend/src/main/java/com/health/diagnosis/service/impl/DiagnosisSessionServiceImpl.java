package com.health.diagnosis.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.health.diagnosis.entity.DiagnosisSession;
import com.health.diagnosis.mapper.DiagnosisSessionMapper;
import com.health.diagnosis.service.DiagnosisSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 问诊会话服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiagnosisSessionServiceImpl implements DiagnosisSessionService {

    private final DiagnosisSessionMapper sessionMapper;

    @Override
    public DiagnosisSession getOrCreate(String sessionKey, Long userId) {
        // 如果前端传了 sessionKey，查找已有会话
        if (StrUtil.isNotBlank(sessionKey)) {
            DiagnosisSession existing = sessionMapper.selectOne(
                    Wrappers.<DiagnosisSession>lambdaQuery()
                            .eq(DiagnosisSession::getSessionKey, sessionKey));
            if (existing != null) {
                log.info("复用已有会话: sessionKey={}, id={}", sessionKey, existing.getId());
                return existing;
            }
            log.warn("前端传入的 sessionKey={} 不存在，将创建新会话", sessionKey);
        }

        // 创建新会话
        DiagnosisSession session = new DiagnosisSession();
        session.setSessionKey(UUID.randomUUID().toString());
        session.setUserId(userId);
        session.setStatus(0);
        session.setMessageCount(0);
        sessionMapper.insert(session);
        log.info("创建新会话: sessionKey={}, id={}", session.getSessionKey(), session.getId());
        return session;
    }

    @Override
    public DiagnosisSession findByKey(String sessionKey) {
        if (StrUtil.isBlank(sessionKey)) return null;
        return sessionMapper.selectOne(
                Wrappers.<DiagnosisSession>lambdaQuery()
                        .eq(DiagnosisSession::getSessionKey, sessionKey));
    }

    @Override
    public void updateSummary(Long sessionId, String summary) {
        DiagnosisSession session = new DiagnosisSession();
        session.setId(sessionId);
        session.setSummary(summary);
        sessionMapper.updateById(session);
    }

    @Override
    public void incrementMessageCount(Long sessionId, int delta) {
        DiagnosisSession session = sessionMapper.selectById(sessionId);
        if (session != null) {
            session.setMessageCount((session.getMessageCount() == null ? 0 : session.getMessageCount()) + delta);
            sessionMapper.updateById(session);
        }
    }

    @Override
    public void completeSession(Long sessionId) {
        DiagnosisSession session = new DiagnosisSession();
        session.setId(sessionId);
        session.setStatus(1);
        sessionMapper.updateById(session);
    }
}
