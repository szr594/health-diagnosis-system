package com.health.diagnosis.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.health.diagnosis.common.BizException;
import com.health.diagnosis.common.PageResult;
import com.health.diagnosis.dto.AppointmentDTO;
import com.health.diagnosis.entity.Appointment;
import com.health.diagnosis.entity.Doctor;
import com.health.diagnosis.mapper.AppointmentMapper;
import com.health.diagnosis.mapper.DoctorMapper;
import com.health.diagnosis.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentMapper appointmentMapper;
    private final DoctorMapper doctorMapper;

    @Override
    public Appointment book(AppointmentDTO dto, Long patientId) {
        Doctor doctor = doctorMapper.selectDoctorDetail(dto.getDoctorId());
        if (doctor == null) {
            throw new BizException(404, "医生不存在");
        }
        if (doctor.getStatus() != null && doctor.getStatus() == 0) {
            throw new BizException(400, "该医生当前已停诊");
        }

        if (dto.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new BizException(400, "预约日期不能早于今天");
        }

        String dateStr = dto.getAppointmentDate().format(DateTimeFormatter.ISO_DATE);
        int booked = appointmentMapper.countByDoctorDateSlot(dto.getDoctorId(), dateStr, dto.getTimeSlot());
        if (doctor.getMaxDailyAppointments() != null && booked >= doctor.getMaxDailyAppointments()) {
            throw new BizException(400, "该时段预约已满，请选择其他时段");
        }

        Appointment appointment = new Appointment();
        appointment.setAppointmentNo(generateNo());
        appointment.setPatientId(patientId);
        appointment.setDoctorId(dto.getDoctorId());
        appointment.setDepartmentId(dto.getDepartmentId());
        appointment.setAppointmentDate(dto.getAppointmentDate());
        appointment.setTimeSlot(dto.getTimeSlot());
        appointment.setReason(dto.getReason());
        appointment.setRemark(dto.getRemark());
        appointment.setStatus(0);
        appointmentMapper.insert(appointment);

        log.info("预约挂号成功: no={}, patient={}, doctor={}",
                appointment.getAppointmentNo(), patientId, dto.getDoctorId());
        return appointmentMapper.selectAppointmentDetail(appointment.getId());
    }

    @Override
    public PageResult<Appointment> pageList(Long patientId, Long doctorId, Integer status, long pageNum, long pageSize) {
        Page<Appointment> page = new Page<>(pageNum, pageSize);
        var result = appointmentMapper.selectAppointmentPage(page, patientId, doctorId, status);
        return PageResult.of(result.getRecords(), result.getTotal(), pageNum, pageSize);
    }

    @Override
    public Appointment detail(Long id) {
        Appointment appointment = appointmentMapper.selectAppointmentDetail(id);
        if (appointment == null) {
            throw new BizException(404, "预约记录不存在");
        }
        return appointment;
    }

    @Override
    public Appointment confirm(Long id, Long doctorUserId) {
        Appointment appointment = getAndValidateDoctorPermission(id, doctorUserId);
        if (appointment.getStatus() != 0) {
            throw new BizException(400, "仅待确认状态可确认");
        }
        appointmentMapper.updateStatus(id, 1, null);
        return appointmentMapper.selectAppointmentDetail(id);
    }

    @Override
    public Appointment complete(Long id, Long doctorUserId) {
        Appointment appointment = getAndValidateDoctorPermission(id, doctorUserId);
        if (appointment.getStatus() != 1) {
            throw new BizException(400, "仅已确认状态可完成就诊");
        }
        appointmentMapper.updateStatus(id, 2, null);
        return appointmentMapper.selectAppointmentDetail(id);
    }

    @Override
    public Appointment cancel(Long id, Long operatorUserId, String reason) {
        Appointment appointment = appointmentMapper.selectAppointmentDetail(id);
        if (appointment == null) {
            throw new BizException(404, "预约记录不存在");
        }
        if (appointment.getStatus() == 3) {
            throw new BizException(400, "该预约已取消");
        }
        if (appointment.getStatus() == 2) {
            throw new BizException(400, "已完成的预约不可取消");
        }
        boolean isPatient = appointment.getPatientId().equals(operatorUserId);
        Doctor doctor = doctorMapper.selectByUserId(operatorUserId);
        boolean isDoctor = doctor != null && doctor.getId().equals(appointment.getDoctorId());
        if (!isPatient && !isDoctor) {
            throw new BizException(403, "无权取消此预约");
        }
        appointmentMapper.updateStatus(id, 3, reason);
        return appointmentMapper.selectAppointmentDetail(id);
    }

    private Appointment getAndValidateDoctorPermission(Long id, Long doctorUserId) {
        Appointment appointment = appointmentMapper.selectAppointmentDetail(id);
        if (appointment == null) {
            throw new BizException(404, "预约记录不存在");
        }
        Doctor doctor = doctorMapper.selectByUserId(doctorUserId);
        if (doctor == null || !doctor.getId().equals(appointment.getDoctorId())) {
            throw new BizException(403, "无权操作此预约");
        }
        return appointment;
    }

    private String generateNo() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int random = ThreadLocalRandom.current().nextInt(100, 1000);
        return "AP" + datePart + random;
    }
}
