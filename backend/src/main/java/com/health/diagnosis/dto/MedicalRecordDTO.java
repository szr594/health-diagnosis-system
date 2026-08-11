package com.health.diagnosis.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 电子病历 DTO（医生填写）。
 */
@Data
public class MedicalRecordDTO {

    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    private Long departmentId;
    private Long appointmentId;

    private String chiefComplaint;
    private String presentIllness;
    private String pastHistory;
    private String physicalExam;
    private String diagnosis;
    private String treatmentPlan;
    private String prescription;
    private String remark;
}
