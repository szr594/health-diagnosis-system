package com.health.diagnosis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.diagnosis.entity.ConsultationRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 问诊记录 Mapper。
 */
@Mapper
public interface ConsultationRecordMapper extends BaseMapper<ConsultationRecord> {
}
