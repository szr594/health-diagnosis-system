package com.health.diagnosis.service.impl;

import com.health.diagnosis.common.BizException;
import com.health.diagnosis.entity.Department;
import com.health.diagnosis.mapper.DepartmentMapper;
import com.health.diagnosis.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentMapper departmentMapper;

    @Override
    public List<Department> listAll() {
        return departmentMapper.selectList(
                com.baomidou.mybatisplus.core.toolkit.Wrappers
                        .<Department>lambdaQuery()
                        .eq(Department::getStatus, 1)
                        .orderByAsc(Department::getSortOrder)
        );
    }

    @Override
    public Department detail(Long id) {
        Department dept = departmentMapper.selectById(id);
        if (dept == null) {
            throw new BizException(404, "科室不存在");
        }
        return dept;
    }

    @Override
    public Department create(Department dept) {
        departmentMapper.insert(dept);
        return dept;
    }

    @Override
    public Department update(Long id, Department dept) {
        Department existing = departmentMapper.selectById(id);
        if (existing == null) {
            throw new BizException(404, "科室不存在");
        }
        dept.setId(id);
        departmentMapper.updateById(dept);
        return departmentMapper.selectById(id);
    }

    @Override
    public void delete(Long id) {
        departmentMapper.deleteById(id);
    }
}
