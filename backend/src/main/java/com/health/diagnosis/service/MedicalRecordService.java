package com.health.diagnosis.service;

import com.health.diagnosis.common.PageResult;
import com.health.diagnosis.dto.MedicalRecordDTO;
import com.health.diagnosis.entity.MedicalRecord;

/**
 * 电子病历服务接口。
 */
public interface MedicalRecordService {

    /**
     * 创建病历（医生操作）。
     */
    MedicalRecord create(MedicalRecordDTO dto, Long doctorUserId);

    /**
     * 分页查询病历。
     * 患者只能查自己的，医生只能查自己接诊的，管理员可查全部。
     */
    PageResult<MedicalRecord> pageList(Long patientId, Long doctorId, Long currentUserId, Integer currentRole, long pageNum, long pageSize);

    MedicalRecord detail(Long id, Long currentUserId, Integer currentRole);
}
