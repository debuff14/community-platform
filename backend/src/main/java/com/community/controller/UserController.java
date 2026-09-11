package com.community.controller;

import com.community.common.LoginUser;
import com.community.common.Result;
import com.community.dto.ChangePasswordDTO;
import com.community.dto.ProfileStatsVO;
import com.community.dto.UpdateProfileDTO;
import com.community.entity.User;
import com.community.interceptor.UserContext;
import com.community.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public Result<User> profile() {
        LoginUser loginUser = UserContext.get();
        return Result.success(userService.getProfile(loginUser.getId()));
    }

    @GetMapping("/stats")
    public Result<ProfileStatsVO> stats() {
        LoginUser loginUser = UserContext.get();
        return Result.success(userService.getStats(loginUser.getId()));
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody UpdateProfileDTO dto) {
        LoginUser loginUser = UserContext.get();
        userService.updateProfile(loginUser.getId(), dto);
        return Result.success("保存成功", null);
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        LoginUser loginUser = UserContext.get();
        userService.changePassword(loginUser.getId(), dto);
        return Result.success("密码修改成功，请重新登录", null);
    }
}
