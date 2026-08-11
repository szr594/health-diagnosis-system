package com.health.diagnosis.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.health.diagnosis.common.BizException;
import com.health.diagnosis.common.JwtUtil;
import com.health.diagnosis.dto.LoginDTO;
import com.health.diagnosis.dto.LoginVO;
import com.health.diagnosis.dto.RegisterDTO;
import com.health.diagnosis.dto.UserProfileDTO;
import com.health.diagnosis.entity.User;
import com.health.diagnosis.mapper.UserMapper;
import com.health.diagnosis.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User register(RegisterDTO dto) {
        Long count = userMapper.selectCount(
                Wrappers.<User>lambdaQuery().eq(User::getUsername, dto.getUsername()));
        if (count != null && count > 0) {
            throw new BizException(400, "用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(StrUtil.isBlank(dto.getNickname()) ? dto.getUsername() : dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setGender(dto.getGender() == null ? 0 : dto.getGender());
        user.setAge(dto.getAge());
        user.setRole(0);
        user.setStatus(1);
        userMapper.insert(user);

        user.setPassword(null);
        log.info("用户注册成功: username={}", user.getUsername());
        return user;
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery().eq(User::getUsername, dto.getUsername()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BizException(400, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException(400, "账号已被禁用，请联系管理员");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        user.setPassword(null);
        vo.setUser(user);
        log.info("用户登录成功: username={}, role={}", user.getUsername(), user.getRole());
        return vo;
    }

    @Override
    public User getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        return user;
    }

    @Override
    public User updateProfile(Long userId, UserProfileDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        if (StrUtil.isNotBlank(dto.getNickname())) {
            user.setNickname(dto.getNickname().trim());
        }
        if (StrUtil.isNotBlank(dto.getRealName())) {
            user.setRealName(dto.getRealName().trim());
        }
        if (StrUtil.isNotBlank(dto.getPhone())) {
            user.setPhone(dto.getPhone().trim());
        }
        if (dto.getGender() != null) {
            user.setGender(dto.getGender());
        }
        if (dto.getAge() != null) {
            user.setAge(dto.getAge());
        }
        if (dto.getHeight() != null) {
            user.setHeight(dto.getHeight());
        }
        if (dto.getWeight() != null) {
            user.setWeight(dto.getWeight());
        }
        if (dto.getAllergyHistory() != null) {
            user.setAllergyHistory(dto.getAllergyHistory());
        }
        if (dto.getMedicalHistory() != null) {
            user.setMedicalHistory(dto.getMedicalHistory());
        }
        userMapper.updateById(user);
        user.setPassword(null);
        return user;
    }
}
