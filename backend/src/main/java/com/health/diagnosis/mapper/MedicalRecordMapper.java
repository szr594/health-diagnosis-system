package com.health.diagnosis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.health.diagnosis.entity.MedicalRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MedicalRecordMapper extends BaseMapper<MedicalRecord> {

    /**
     * 分页查询病历列表（联表回填名称）。
     */
    @Select("""
        SELECT m.*,
               pu.real_name AS patient_name,
               du.real_name AS doctor_name,
               dept.name    AS department_name
        FROM t_medical_record m
        LEFT JOIN t_user pu      ON pu.id = m.patient_id
        LEFT JOIN t_doctor doc   ON doc.id = m.doctor_id
        LEFT JOIN t_user du      ON du.id = doc.user_id
        LEFT JOIN t_department dept ON dept.id = m.department_id
        WHERE (#{patientId} IS NULL OR m.patient_id = #{patientId})
          AND (#{doctorId} IS NULL OR m.doctor_id = #{doctorId})
        ORDER BY m.id DESC
        """)
    IPage<MedicalRecord> selectRecordPage(Page<MedicalRecord> page,
                                          @Param("patientId") Long patientId,
                                          @Param("doctorId") Long doctorId);

    /**
     * 查询病历详情（含关联名称）。
     */
    @Select("""
        SELECT m.*,
               pu.real_name AS patient_name,
               du.real_name AS doctor_name,
               dept.name    AS department_name
        FROM t_medical_record m
        LEFT JOIN t_user pu      ON pu.id = m.patient_id
        LEFT JOIN t_doctor doc   ON doc.id = m.doctor_id
        LEFT JOIN t_user du      ON du.id = doc.user_id
        LEFT JOIN t_department dept ON dept.id = m.department_id
        WHERE m.id = #{id}
        """)
    MedicalRecord selectRecordDetail(@Param("id") Long id);
}
