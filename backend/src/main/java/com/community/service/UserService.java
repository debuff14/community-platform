package com.community.service;

import com.community.dto.ChangePasswordDTO;
import com.community.dto.LoginDTO;
import com.community.dto.LoginVO;
import com.community.dto.ProfileStatsVO;
import com.community.dto.RegisterDTO;
import com.community.dto.UpdateProfileDTO;
import com.community.entity.User;

public interface UserService {

    boolean isUsernameAvailable(String username);

    void register(RegisterDTO dto);

    LoginVO login(LoginDTO dto);

    User getProfile(Long userId);

    ProfileStatsVO getStats(Long userId);

    void updateProfile(Long userId, UpdateProfileDTO dto);

    void changePassword(Long userId, ChangePasswordDTO dto);
}
