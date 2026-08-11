package com.health.diagnosis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.diagnosis.entity.Department;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
}
