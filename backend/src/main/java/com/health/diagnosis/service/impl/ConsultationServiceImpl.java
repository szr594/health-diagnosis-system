package com.health.diagnosis.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.health.diagnosis.common.BizException;
import com.health.diagnosis.common.PageResult;
import com.health.diagnosis.dto.AiDiagnosisResponse;
import com.health.diagnosis.dto.ConsultationRequest;
import com.health.diagnosis.dto.ConsultationVO;
import com.health.diagnosis.dto.HotQuestionVO;
import com.health.diagnosis.entity.ConsultationRecord;
import com.health.diagnosis.entity.DiagnosisSession;
import com.health.diagnosis.entity.User;
import com.health.diagnosis.mapper.ConsultationRecordMapper;
import com.health.diagnosis.mapper.DiagnosisSessionMapper;
import com.health.diagnosis.mapper.UserMapper;
import com.health.diagnosis.service.ChatHistoryService;
import com.health.diagnosis.service.ConsultationService;
import com.health.diagnosis.service.DiagnosisSessionService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultationServiceImpl implements ConsultationService {

    private static final String HOT_KEY = "ai:diag:hot";
    private static final String CACHE_KEY_PREFIX = "ai:diag:cache:";
    private static final int MAX_HISTORY_MESSAGES = 10;
    private static final int SUMMARY_THRESHOLD = 20;

    private final ConsultationRecordMapper recordMapper;
    private final DiagnosisSessionMapper sessionMapper;
    private final UserMapper userMapper;
    private final ChatHistoryService chatHistoryService;
    private final DiagnosisSessionService sessionService;
    private final RestTemplate restTemplate;
    private final Optional<RedisTemplate<String, Object>> redisTemplate;
    private final ObjectMapper objectMapper;

    @Resource(name = "aiExecutor")
    private Executor aiExecutor;

    @Value("${health.ai.base-url}")
    private String aiBaseUrl;

    @Value("${health.ai.timeout-ms}")
    private long aiTimeoutMs;

    @Value("${health.ai.fallback-enabled}")
    private boolean fallbackEnabled;

    @Value("${health.redis.cache-ttl}")
    private long cacheTtl;

    @Override
    public ConsultationVO preDiagnosis(ConsultationRequest request, Long userId) {
        boolean isMultiTurn = StrUtil.isNotBlank(request.getSessionId());

        if (isMultiTurn) {
            return preDiagnosisMultiTurn(request, userId);
        }

        String cacheKey = CACHE_KEY_PREFIX + DigestUtil.md5Hex(normalizeSymptom(request.getSymptomDescription()));
        if (redisTemplate.isPresent()) {
            Object cached = redisTemplate.get().opsForValue().get(cacheKey);
            if (cached != null) {
                log.info("预问诊结果缓存命中: {}", cacheKey);
                return buildRecordFromCache(request, userId, String.valueOf(cached));
            }
        }

        DiagnosisSession session = sessionService.getOrCreate(null, userId);
        ConsultationRecord record = createRecord(request, userId, session.getId());
        chatHistoryService.saveWithSession(userId, record.getId(), session.getId(), "user", request.getSymptomDescription());
        sessionService.incrementMessageCount(session.getId(), 1);

        AiDiagnosisResponse aiResp = callAiWithTimeout(request, null, null);

        applyAiResultAndFinish(record, aiResp, session, userId);

        cacheAndRank(request, aiResp);

        return toVO(record, session);
    }

    private ConsultationVO preDiagnosisMultiTurn(ConsultationRequest request, Long userId) {
        DiagnosisSession session = sessionService.findByKey(request.getSessionId());
        if (session == null) {
            log.warn("前端 sessionKey={} 无效，创建新会话", request.getSessionId());
            session = sessionService.getOrCreate(null, userId);
        }

        String historyText = chatHistoryService.buildHistoryText(session.getId(), MAX_HISTORY_MESSAGES);
        String sessionSummary = null;
        if (session.getMessageCount() != null && session.getMessageCount() > SUMMARY_THRESHOLD) {
            sessionSummary = session.getSummary();
        }

        ConsultationRecord record = createRecord(request, userId, session.getId());
        chatHistoryService.saveWithSession(userId, record.getId(), session.getId(), "user", request.getSymptomDescription());
        sessionService.incrementMessageCount(session.getId(), 1);

        AiDiagnosisResponse aiResp = callAiWithTimeout(request, historyText, sessionSummary);

        applyAiResultAndFinish(record, aiResp, session, userId);

        cacheAndRank(request, aiResp);

        return toVO(record, session);
    }

    @Override
    public PageResult<ConsultationVO> pageList(Long userId, long pageNum, long pageSize) {
        IPage<ConsultationRecord> page = recordMapper.selectPage(
                new Page<>(pageNum, pageSize),
                Wrappers.<ConsultationRecord>lambdaQuery()
                        .eq(ConsultationRecord::getUserId, userId)
                        .orderByDesc(ConsultationRecord::getCreateTime));
        List<ConsultationVO> records = new ArrayList<>();
        for (ConsultationRecord record : page.getRecords()) {
            DiagnosisSession session = record.getSessionId() != null
                    ? sessionMapper.selectById(record.getSessionId()) : null;
            records.add(toVO(record, session));
        }
        PageResult<ConsultationVO> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setRecords(records);
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        return result;
    }

    @Override
    public ConsultationVO detail(Long id, Long userId) {
        ConsultationRecord record = getOwnedRecord(id, userId);
        DiagnosisSession session = record.getSessionId() != null
                ? findSessionById(record.getSessionId()) : null;
        return toVO(record, session);
    }

    @Override
    public void delete(Long id, Long userId) {
        ConsultationRecord record = getOwnedRecord(id, userId);
        recordMapper.deleteById(record.getId());
        log.info("删除问诊记录: id={}", id);
    }

    @Override
    public List<HotQuestionVO> hotQuestions(int topN) {
        List<HotQuestionVO> result = new ArrayList<>();
        if (redisTemplate.isEmpty()) {
            return result;
        }
        Set<ZSetOperations.TypedTuple<Object>> tuples =
                redisTemplate.get().opsForZSet().reverseRangeWithScores(HOT_KEY, 0, topN - 1L);
        if (tuples != null) {
            for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
                if (tuple.getValue() == null) {
                    continue;
                }
                HotQuestionVO vo = new HotQuestionVO();
                vo.setSymptom(String.valueOf(tuple.getValue()));
                vo.setCount(tuple.getScore());
                result.add(vo);
            }
        }
        return result;
    }

    private ConsultationRecord createRecord(ConsultationRequest request, Long userId, Long sessionId) {
        ConsultationRecord record = new ConsultationRecord();
        record.setUserId(userId);
        record.setSessionId(sessionId);
        record.setSymptomDescription(request.getSymptomDescription());
        record.setSymptomDuration(request.getSymptomDuration());
        record.setChiefComplaint(buildChiefComplaint(request));
        record.setStatus(0);
        recordMapper.insert(record);
        return record;
    }

    private AiDiagnosisResponse callAiWithTimeout(ConsultationRequest request, String historyText, String summary) {
        CompletableFuture<AiDiagnosisResponse> future = CompletableFuture.supplyAsync(
                () -> callAiService(request, historyText, summary), aiExecutor);

        try {
            return future.get(aiTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            log.warn("AI 服务调用超时（{}ms）", aiTimeoutMs);
            throw new BizException("AI 服务响应超时，请稍后重试");
        } catch (Exception e) {
            log.error("AI 服务调用异常", e);
            throw new BizException("AI 服务暂不可用，已降级处理");
        }
    }

    private AiDiagnosisResponse callAiService(ConsultationRequest request, String historyText, String summary) {
        String url = aiBaseUrl + "/api/ai/diagnosis";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("symptom_description", request.getSymptomDescription());
        body.put("symptom_duration", request.getSymptomDuration());
        body.put("age", request.getAge());
        body.put("gender", request.getGender());
        body.put("medical_history", request.getMedicalHistory());
        body.put("allergy_history", request.getAllergyHistory());

        if (StrUtil.isNotBlank(historyText)) {
            body.put("conversation_history", historyText);
        }
        if (StrUtil.isNotBlank(summary)) {
            body.put("conversation_summary", summary);
        }

        ResponseEntity<AiDiagnosisResponse> response =
                restTemplate.postForEntity(url, new HttpEntity<>(body, headers), AiDiagnosisResponse.class);
        AiDiagnosisResponse respBody = response.getBody();
        if (respBody == null || respBody.getData() == null) {
            throw new BizException("AI 服务返回数据为空");
        }
        return respBody;
    }

    private void applyAiResultAndFinish(ConsultationRecord record, AiDiagnosisResponse resp,
                                         DiagnosisSession session, Long userId) {
        applyAiResult(record, resp);
        record.setStatus(1);
        recordMapper.updateById(record);
        chatHistoryService.saveWithSession(userId, record.getId(), session.getId(), "assistant", resp.getData().getAdvice());
        sessionService.incrementMessageCount(session.getId(), 1);
    }

    private void applyAiResult(ConsultationRecord record, AiDiagnosisResponse resp) {
        AiDiagnosisResponse.DiagnosisData data = resp.getData();
        record.setRiskLevel(data.getRiskLevel());
        record.setPossibleDiseases(data.getPossibleDiseases() == null
                ? null : String.join(",", data.getPossibleDiseases()));
        record.setSuggestedDepartment(data.getSuggestedDepartment());
        record.setAiAdvice(data.getAdvice());
        record.setStructuredAdvice(JSONUtil.toJsonStr(data));
    }

    private void cacheAndRank(ConsultationRequest request, AiDiagnosisResponse aiResp) {
        if (redisTemplate.isEmpty()) {
            return;
        }
        String cacheKey = CACHE_KEY_PREFIX + DigestUtil.md5Hex(normalizeSymptom(request.getSymptomDescription()));
        redisTemplate.get().opsForValue().set(cacheKey, JSONUtil.toJsonStr(aiResp.getData()), Duration.ofSeconds(cacheTtl));
        redisTemplate.get().opsForZSet().incrementScore(HOT_KEY, normalizeSymptom(request.getSymptomDescription()), 1);
    }

    private ConsultationVO degrade(ConsultationRecord record, String reason) {
        record.setStatus(2);
        record.setFailReason(reason);
        recordMapper.updateById(record);
        ConsultationVO vo = toVO(record, null);
        if (fallbackEnabled) {
            vo.setAiAdvice("抱歉，AI 预问诊服务暂时不可用（" + reason + "）。"
                    + "建议您密切关注症状变化，若症状持续或加重，请及时前往医院全科门诊就诊，"
                    + "由执业医师进行专业评估。");
        }
        return vo;
    }

    private ConsultationVO buildRecordFromCache(ConsultationRequest request, Long userId, String cachedJson) {
        DiagnosisSession session = sessionService.getOrCreate(null, userId);
        ConsultationRecord record = new ConsultationRecord();
        record.setUserId(userId);
        record.setSessionId(session.getId());
        record.setSymptomDescription(request.getSymptomDescription());
        record.setSymptomDuration(request.getSymptomDuration());
        record.setChiefComplaint(buildChiefComplaint(request));
        record.setStatus(1);
        record.setAiAdvice(null);
        record.setStructuredAdvice(cachedJson);
        recordMapper.insert(record);
        try {
            AiDiagnosisResponse.DiagnosisData data =
                    objectMapper.readValue(cachedJson, AiDiagnosisResponse.DiagnosisData.class);
            record.setRiskLevel(data.getRiskLevel());
            record.setPossibleDiseases(data.getPossibleDiseases() == null
                    ? null : String.join(",", data.getPossibleDiseases()));
            record.setSuggestedDepartment(data.getSuggestedDepartment());
            record.setAiAdvice(data.getAdvice());
            recordMapper.updateById(record);
        } catch (Exception e) {
            log.warn("缓存结果解析失败: {}", e.getMessage());
        }
        chatHistoryService.saveWithSession(userId, record.getId(), session.getId(), "user", request.getSymptomDescription());
        chatHistoryService.saveWithSession(userId, record.getId(), session.getId(), "assistant", record.getAiAdvice());
        sessionService.incrementMessageCount(session.getId(), 2);
        return toVO(record, session);
    }

    private DiagnosisSession findSessionById(Long sessionId) {
        return sessionMapper.selectById(sessionId);
    }

    private ConsultationRecord getOwnedRecord(Long id, Long userId) {
        ConsultationRecord record = recordMapper.selectById(id);
        if (record == null) {
            throw new BizException(404, "问诊记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new BizException(403, "无权访问该问诊记录");
        }
        return record;
    }

    private ConsultationVO toVO(ConsultationRecord record, DiagnosisSession session) {
        ConsultationVO vo = new ConsultationVO();
        vo.setId(record.getId());
        vo.setUserId(record.getUserId());
        vo.setSessionId(record.getSessionId());
        vo.setSessionKey(session != null ? session.getSessionKey() : null);
        vo.setSymptomDescription(record.getSymptomDescription());
        vo.setSymptomDuration(record.getSymptomDuration());
        vo.setAiAdvice(record.getAiAdvice());
        vo.setStructuredAdvice(record.getStructuredAdvice());
        vo.setRiskLevel(record.getRiskLevel());
        vo.setPossibleDiseases(record.getPossibleDiseases());
        vo.setSuggestedDepartment(record.getSuggestedDepartment());
        vo.setStatus(record.getStatus());
        vo.setFailReason(record.getFailReason());
        vo.setCreateTime(record.getCreateTime());
        User user = userMapper.selectById(record.getUserId());
        vo.setNickname(user != null ? user.getNickname() : null);
        return vo;
    }

    private String buildChiefComplaint(ConsultationRequest request) {
        String symptom = StrUtil.trim(request.getSymptomDescription());
        return symptom.length() <= 30 ? symptom : symptom.substring(0, 30);
    }

    private String normalizeSymptom(String symptom) {
        return StrUtil.trim(symptom).toLowerCase();
    }
}
