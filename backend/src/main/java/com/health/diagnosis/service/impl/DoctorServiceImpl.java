package com.health.diagnosis.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.health.diagnosis.common.BizException;
import com.health.diagnosis.common.PageResult;
import com.health.diagnosis.dto.DoctorDTO;
import com.health.diagnosis.entity.Doctor;
import com.health.diagnosis.mapper.DoctorMapper;
import com.health.diagnosis.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorMapper doctorMapper;

    @Override
    public PageResult<Doctor> pageList(Long departmentId, String keyword, long pageNum, long pageSize) {
        Page<Doctor> page = new Page<>(pageNum, pageSize);
        var result = doctorMapper.selectDoctorPage(page, departmentId, keyword);
        return PageResult.of(result.getRecords(), result.getTotal(), pageNum, pageSize);
    }

    @Override
    public Doctor detail(Long id) {
        Doctor doctor = doctorMapper.selectDoctorDetail(id);
        if (doctor == null) {
            throw new BizException(404, "医生不存在");
        }
        return doctor;
    }

    @Override
    public Doctor create(DoctorDTO dto) {
        Doctor existing = doctorMapper.selectByUserId(Long.valueOf(dto.getUserId()));
        if (existing != null) {
            throw new BizException(400, "该用户已绑定医生档案");
        }
        Doctor doctor = new Doctor();
        doctor.setUserId(Long.valueOf(dto.getUserId()));
        doctor.setDepartmentId(dto.getDepartmentId());
        doctor.setTitle(dto.getTitle());
        doctor.setSpecialty(dto.getSpecialty());
        doctor.setDescription(dto.getDescription());
        doctor.setConsultationFee(dto.getConsultationFee() != null ? dto.getConsultationFee() : BigDecimal.ZERO);
        doctor.setMaxDailyAppointments(dto.getMaxDailyAppointments() != null ? dto.getMaxDailyAppointments() : 20);
        doctor.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        doctorMapper.insert(doctor);
        return doctorMapper.selectDoctorDetail(doctor.getId());
    }

    @Override
    public Doctor update(Long id, DoctorDTO dto) {
        Doctor existing = doctorMapper.selectById(id);
        if (existing == null) {
            throw new BizException(404, "医生不存在");
        }
        existing.setDepartmentId(dto.getDepartmentId());
        existing.setTitle(dto.getTitle());
        existing.setSpecialty(dto.getSpecialty());
        existing.setDescription(dto.getDescription());
        if (dto.getConsultationFee() != null) {
            existing.setConsultationFee(dto.getConsultationFee());
        }
        if (dto.getMaxDailyAppointments() != null) {
            existing.setMaxDailyAppointments(dto.getMaxDailyAppointments());
        }
        if (dto.getStatus() != null) {
            existing.setStatus(dto.getStatus());
        }
        doctorMapper.updateById(existing);
        return doctorMapper.selectDoctorDetail(id);
    }

    @Override
    public void delete(Long id) {
        doctorMapper.deleteById(id);
    }

    @Override
    public Doctor getByUserId(Long userId) {
        return doctorMapper.selectByUserId(userId);
    }
}
