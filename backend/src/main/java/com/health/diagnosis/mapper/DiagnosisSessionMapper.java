package com.health.diagnosis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.diagnosis.entity.DiagnosisSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * 问诊会话 Mapper。
 */
@Mapper
public interface DiagnosisSessionMapper extends BaseMapper<DiagnosisSession> {
}
