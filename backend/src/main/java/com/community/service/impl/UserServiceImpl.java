package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.exception.BusinessException;
import com.community.dto.ChangePasswordDTO;
import com.community.dto.LoginDTO;
import com.community.dto.LoginVO;
import com.community.dto.ProfileStatsVO;
import com.community.dto.RegisterDTO;
import com.community.dto.UpdateProfileDTO;
import com.community.entity.User;
import com.community.mapper.BillMapper;
import com.community.mapper.RepairOrderMapper;
import com.community.mapper.UserMapper;
import com.community.service.UserService;
import com.community.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.YearMonth;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String FAIL_KEY_PREFIX = "login:fail:";
    private static final String LOCK_KEY_PREFIX = "login:lock:";
    private static final int MAX_FAIL_COUNT = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(10);

    private final UserMapper userMapper;
    private final BillMapper billMapper;
    private final RepairOrderMapper repairOrderMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final StringRedisTemplate stringRedisTemplate;
    private final JwtUtil jwtUtil;

    @Override
    public boolean isUsernameAvailable(String username) {
        return userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) == 0;
    }

    @Override
    public void register(RegisterDTO dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
        if (!isUsernameAvailable(dto.getUsername())) {
            throw new BusinessException("该用户名已被占用");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setBuilding(dto.getBuilding());
        user.setRoomNo(dto.getRoomNo());
        user.setRole(0);
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("该用户名已被占用");
        }
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        String lockKey = LOCK_KEY_PREFIX + dto.getUsername();
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(lockKey))) {
            Long seconds = stringRedisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
            long minutes = seconds == null || seconds <= 0 ? 1 : (seconds + 59) / 60;
            throw new BusinessException("账号已锁定，请 " + minutes + " 分钟后再试");
        }

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            countLoginFail(dto.getUsername());
            throw new BusinessException("账号或密码错误");
        }

        stringRedisTemplate.delete(FAIL_KEY_PREFIX + dto.getUsername());
        stringRedisTemplate.delete(lockKey);

        LoginVO vo = new LoginVO();
        vo.setToken(jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole()));
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRole(user.getRole());
        vo.setPhone(user.getPhone());
        vo.setBuilding(user.getBuilding());
        vo.setRoomNo(user.getRoomNo());
        return vo;
    }

    private void countLoginFail(String username) {
        String failKey = FAIL_KEY_PREFIX + username;
        Long count = stringRedisTemplate.opsForValue().increment(failKey);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(failKey, LOCK_DURATION);
        }
        if (count != null && count >= MAX_FAIL_COUNT) {
            stringRedisTemplate.opsForValue().set(LOCK_KEY_PREFIX + username, "1", LOCK_DURATION);
            stringRedisTemplate.delete(failKey);
            throw new BusinessException("账号已锁定，请 " + LOCK_DURATION.toMinutes() + " 分钟后再试");
        }
    }

    @Override
    public User getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    @Override
    public ProfileStatsVO getStats(Long userId) {
        ProfileStatsVO vo = new ProfileStatsVO();
        vo.setUnpaidAmount(billMapper.sumUnpaidAmount(userId, YearMonth.now().toString()));
        vo.setUnhandledRepairCount(repairOrderMapper.countUnhandled(userId));
        return vo;
    }

    @Override
    public void updateProfile(Long userId, UpdateProfileDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPhone(dto.getPhone());
        user.setRoomNo(dto.getRoomNo());
        userMapper.updateById(user);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
        if (dto.getNewPassword().equals(dto.getOldPassword())) {
            throw new BusinessException("新密码不能与原密码相同");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }
}
