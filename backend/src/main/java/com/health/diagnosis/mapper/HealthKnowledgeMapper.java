package com.health.diagnosis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.diagnosis.entity.HealthKnowledge;
import org.apache.ibatis.annotations.Mapper;

/**
 * 健康知识文档 Mapper。
 */
@Mapper
public interface HealthKnowledgeMapper extends BaseMapper<HealthKnowledge> {
}
