package com.health.diagnosis.service;

import com.health.diagnosis.dto.LoginDTO;
import com.health.diagnosis.dto.LoginVO;
import com.health.diagnosis.dto.RegisterDTO;
import com.health.diagnosis.dto.UserProfileDTO;
import com.health.diagnosis.entity.User;

/**
 * 用户服务接口。
 */
public interface UserService {

    /**
     * 用户注册。
     */
    User register(RegisterDTO dto);

    /**
     * 用户登录，返回令牌与用户信息。
     */
    LoginVO login(LoginDTO dto);

    /**
     * 根据 ID 查询用户。
     */
    User getUserById(Long userId);

    /**
     * 更新当前用户资料。
     */
    User updateProfile(Long userId, UserProfileDTO dto);
}
