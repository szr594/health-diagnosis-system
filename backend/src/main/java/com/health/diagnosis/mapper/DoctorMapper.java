package com.health.diagnosis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.health.diagnosis.entity.Doctor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {

    /**
     * 分页查询医生列表（联表回填姓名/科室名/头像）。
     */
    @Select("""
        SELECT d.*,
               u.real_name AS doctor_name,
               u.avatar    AS avatar,
               dept.name   AS department_name
        FROM t_doctor d
        LEFT JOIN t_user u      ON u.id = d.user_id
        LEFT JOIN t_department dept ON dept.id = d.department_id
        WHERE (#{departmentId} IS NULL OR d.department_id = #{departmentId})
          AND (#{keyword} IS NULL OR u.real_name LIKE CONCAT('%', #{keyword}, '%')
                                   OR d.specialty LIKE CONCAT('%', #{keyword}, '%'))
        ORDER BY d.id DESC
        """)
    IPage<Doctor> selectDoctorPage(Page<Doctor> page,
                                   @Param("departmentId") Long departmentId,
                                   @Param("keyword") String keyword);

    /**
     * 根据 ID 查询医生详情（含关联名称）。
     */
    @Select("""
        SELECT d.*,
               u.real_name AS doctor_name,
               u.avatar    AS avatar,
               dept.name   AS department_name
        FROM t_doctor d
        LEFT JOIN t_user u      ON u.id = d.user_id
        LEFT JOIN t_department dept ON dept.id = d.department_id
        WHERE d.id = #{id}
        """)
    Doctor selectDoctorDetail(@Param("id") Long id);

    /**
     * 根据用户ID查询医生记录。
     */
    @Select("SELECT * FROM t_doctor WHERE user_id = #{userId}")
    Doctor selectByUserId(@Param("userId") Long userId);
}
