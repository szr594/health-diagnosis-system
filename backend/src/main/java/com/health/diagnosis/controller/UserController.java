package com.health.diagnosis.controller;

import com.health.diagnosis.common.Result;
import com.health.diagnosis.common.UserContext;
import com.health.diagnosis.dto.LoginDTO;
import com.health.diagnosis.dto.LoginVO;
import com.health.diagnosis.dto.RegisterDTO;
import com.health.diagnosis.dto.UserProfileDTO;
import com.health.diagnosis.entity.User;
import com.health.diagnosis.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<User> register(@RequestBody @Valid RegisterDTO dto) {
        return Result.success(userService.register(dto));
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    @GetMapping("/info")
    public Result<User> info() {
        Long userId = UserContext.getUserId();
        User user = userService.getUserById(userId);
        user.setPassword(null);
        return Result.success(user);
    }

    @PostMapping("/update")
    public Result<User> update(@RequestBody @Valid UserProfileDTO dto) {
        Long userId = UserContext.getUserId();
        User user = userService.updateProfile(userId, dto);
        return Result.success(user);
    }
}
