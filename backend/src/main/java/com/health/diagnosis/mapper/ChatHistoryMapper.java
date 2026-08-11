package com.health.diagnosis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.diagnosis.entity.ChatHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 问诊对话历史 Mapper。
 */
@Mapper
public interface ChatHistoryMapper extends BaseMapper<ChatHistory> {
}
