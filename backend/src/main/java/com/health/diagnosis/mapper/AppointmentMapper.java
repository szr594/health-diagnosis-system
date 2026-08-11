package com.health.diagnosis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.health.diagnosis.entity.Appointment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {

    /**
     * 分页查询预约列表（联表回填患者/医生/科室名称）。
     */
    @Select("""
        SELECT a.*,
               pu.real_name AS patient_name,
               du.real_name AS doctor_name,
               dept.name    AS department_name
        FROM t_appointment a
        LEFT JOIN t_user pu      ON pu.id = a.patient_id
        LEFT JOIN t_doctor doc   ON doc.id = a.doctor_id
        LEFT JOIN t_user du      ON du.id = doc.user_id
        LEFT JOIN t_department dept ON dept.id = a.department_id
        WHERE (#{patientId} IS NULL OR a.patient_id = #{patientId})
          AND (#{doctorId} IS NULL OR a.doctor_id = #{doctorId})
          AND (#{status} IS NULL OR a.status = #{status})
        ORDER BY a.appointment_date DESC, a.id DESC
        """)
    IPage<Appointment> selectAppointmentPage(Page<Appointment> page,
                                             @Param("patientId") Long patientId,
                                             @Param("doctorId") Long doctorId,
                                             @Param("status") Integer status);

    /**
     * 查询预约详情（含关联名称）。
     */
    @Select("""
        SELECT a.*,
               pu.real_name AS patient_name,
               du.real_name AS doctor_name,
               dept.name    AS department_name
        FROM t_appointment a
        LEFT JOIN t_user pu      ON pu.id = a.patient_id
        LEFT JOIN t_doctor doc   ON doc.id = a.doctor_id
        LEFT JOIN t_user du      ON du.id = doc.user_id
        LEFT JOIN t_department dept ON dept.id = a.department_id
        WHERE a.id = #{id}
        """)
    Appointment selectAppointmentDetail(@Param("id") Long id);

    /**
     * 统计某医生某日某时段已预约数量。
     */
    @Select("""
        SELECT COUNT(*) FROM t_appointment
        WHERE doctor_id = #{doctorId}
          AND appointment_date = #{date}
          AND time_slot = #{timeSlot}
          AND status IN (0, 1)
        """)
    int countByDoctorDateSlot(@Param("doctorId") Long doctorId,
                              @Param("date") String date,
                              @Param("timeSlot") String timeSlot);

    /**
     * 更新预约状态。
     */
    @Update("UPDATE t_appointment SET status = #{status}, cancel_reason = #{cancelReason} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id,
                     @Param("status") Integer status,
                     @Param("cancelReason") String cancelReason);
}
