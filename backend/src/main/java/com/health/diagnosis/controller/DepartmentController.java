package com.health.diagnosis.controller;

import com.health.diagnosis.common.Result;
import com.health.diagnosis.entity.Department;
import com.health.diagnosis.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/department")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping("/list")
    public Result<List<Department>> list() {
        return Result.success(departmentService.listAll());
    }

    @GetMapping("/detail/{id}")
    public Result<Department> detail(@PathVariable Long id) {
        return Result.success(departmentService.detail(id));
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Department> create(@RequestBody Department dept) {
        return Result.success(departmentService.create(dept));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Department> update(@PathVariable Long id, @RequestBody Department dept) {
        return Result.success(departmentService.update(id, dept));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return Result.success();
    }
}
