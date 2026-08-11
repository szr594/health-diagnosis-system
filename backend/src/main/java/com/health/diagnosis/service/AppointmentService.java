package com.health.diagnosis.service;

import com.health.diagnosis.common.PageResult;
import com.health.diagnosis.dto.AppointmentDTO;
import com.health.diagnosis.entity.Appointment;

/**
 * 预约挂号服务接口。
 */
public interface AppointmentService {

    /**
     * 患者预约挂号。
     */
    Appointment book(AppointmentDTO dto, Long patientId);

    /**
     * 分页查询预约列表（患者视角 / 医生视角 / 管理员视角）。
     */
    PageResult<Appointment> pageList(Long patientId, Long doctorId, Integer status, long pageNum, long pageSize);

    Appointment detail(Long id);

    /**
     * 确认预约（医生操作）。
     */
    Appointment confirm(Long id, Long doctorUserId);

    /**
     * 完成预约（医生操作，标记就诊完成）。
     */
    Appointment complete(Long id, Long doctorUserId);

    /**
     * 取消预约（患者或医生操作）。
     */
    Appointment cancel(Long id, Long operatorUserId, String reason);
}
