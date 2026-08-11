package com.health.diagnosis.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.health.diagnosis.common.BizException;
import com.health.diagnosis.common.PageResult;
import com.health.diagnosis.common.RoleType;
import com.health.diagnosis.dto.MedicalRecordDTO;
import com.health.diagnosis.entity.Doctor;
import com.health.diagnosis.entity.MedicalRecord;
import com.health.diagnosis.mapper.DoctorMapper;
import com.health.diagnosis.mapper.MedicalRecordMapper;
import com.health.diagnosis.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordMapper medicalRecordMapper;
    private final DoctorMapper doctorMapper;

    @Override
    public MedicalRecord create(MedicalRecordDTO dto, Long doctorUserId) {
        Doctor doctor = doctorMapper.selectByUserId(doctorUserId);
        if (doctor == null) {
            throw new BizException(403, "当前用户无医生档案，无法创建病历");
        }

        MedicalRecord record = new MedicalRecord();
        record.setRecordNo(generateNo());
        record.setPatientId(dto.getPatientId());
        record.setDoctorId(doctor.getId());
        record.setDepartmentId(dto.getDepartmentId() != null ? dto.getDepartmentId() : doctor.getDepartmentId());
        record.setAppointmentId(dto.getAppointmentId());
        record.setChiefComplaint(dto.getChiefComplaint());
        record.setPresentIllness(dto.getPresentIllness());
        record.setPastHistory(dto.getPastHistory());
        record.setPhysicalExam(dto.getPhysicalExam());
        record.setDiagnosis(dto.getDiagnosis());
        record.setTreatmentPlan(dto.getTreatmentPlan());
        record.setPrescription(dto.getPrescription());
        record.setRemark(dto.getRemark());
        medicalRecordMapper.insert(record);

        log.info("电子病历创建成功: no={}, patient={}, doctor={}",
                record.getRecordNo(), dto.getPatientId(), doctor.getId());
        return medicalRecordMapper.selectRecordDetail(record.getId());
    }

    @Override
    public PageResult<MedicalRecord> pageList(Long patientId, Long doctorId, Long currentUserId, Integer currentRole, long pageNum, long pageSize) {
        RoleType role = RoleType.fromCode(currentRole);
        Long queryPatientId = patientId;
        Long queryDoctorId = doctorId;

        if (role == RoleType.PATIENT) {
            queryPatientId = currentUserId;
        } else if (role == RoleType.DOCTOR) {
            Doctor doctor = doctorMapper.selectByUserId(currentUserId);
            if (doctor != null) {
                queryDoctorId = doctor.getId();
            }
        }

        Page<MedicalRecord> page = new Page<>(pageNum, pageSize);
        var result = medicalRecordMapper.selectRecordPage(page, queryPatientId, queryDoctorId);
        return PageResult.of(result.getRecords(), result.getTotal(), pageNum, pageSize);
    }

    @Override
    public MedicalRecord detail(Long id, Long currentUserId, Integer currentRole) {
        MedicalRecord record = medicalRecordMapper.selectRecordDetail(id);
        if (record == null) {
            throw new BizException(404, "病历不存在");
        }

        RoleType role = RoleType.fromCode(currentRole);
        if (role == RoleType.PATIENT) {
            if (!record.getPatientId().equals(currentUserId)) {
                throw new BizException(403, "无权查看此病历");
            }
        } else if (role == RoleType.DOCTOR) {
            Doctor doctor = doctorMapper.selectByUserId(currentUserId);
            if (doctor == null || !doctor.getId().equals(record.getDoctorId())) {
                throw new BizException(403, "无权查看此病历");
            }
        }

        return record;
    }

    private String generateNo() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int random = ThreadLocalRandom.current().nextInt(100, 1000);
        return "MR" + datePart + random;
    }
}
