package com.health.diagnosis.service;

import com.health.diagnosis.common.PageResult;
import com.health.diagnosis.dto.DoctorDTO;
import com.health.diagnosis.entity.Doctor;

/**
 * 医生服务接口。
 */
public interface DoctorService {

    PageResult<Doctor> pageList(Long departmentId, String keyword, long pageNum, long pageSize);

    Doctor detail(Long id);

    Doctor create(DoctorDTO dto);

    Doctor update(Long id, DoctorDTO dto);

    void delete(Long id);

    /**
     * 根据当前登录用户ID查询其医生档案。
     */
    Doctor getByUserId(Long userId);
}
