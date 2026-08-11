package com.health.diagnosis.service;

import com.health.diagnosis.entity.Department;

import java.util.List;

/**
 * 科室服务接口。
 */
public interface DepartmentService {

    List<Department> listAll();

    Department detail(Long id);

    Department create(Department dept);

    Department update(Long id, Department dept);

    void delete(Long id);
}
