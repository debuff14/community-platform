package com.community.controller;

import com.community.common.Result;
import com.community.dto.LoginDTO;
import com.community.dto.LoginVO;
import com.community.dto.RegisterDTO;
import com.community.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success("注册成功", null);
    }

    @GetMapping("/check-username")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        return Result.success(userService.isUsernameAvailable(username));
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success("登录成功", userService.login(dto));
    }
}
