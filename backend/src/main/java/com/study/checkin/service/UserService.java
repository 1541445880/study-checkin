package com.study.checkin.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.study.checkin.dto.LoginRequest;
import com.study.checkin.dto.RegisterRequest;
import com.study.checkin.dto.UpdatePasswordRequest;
import com.study.checkin.dto.UpdateProfileRequest;
import com.study.checkin.entity.User;
import com.study.checkin.exception.BusinessException;
import com.study.checkin.mapper.UserMapper;
import com.study.checkin.util.JwtUtil;
import com.study.checkin.vo.LoginVO;
import com.study.checkin.vo.UserProfileVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public void register(RegisterRequest req) {
        Long count = userMapper.selectCount(new QueryWrapper<User>().eq("username", req.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(encoder.encode(req.getPassword()));
        String nickname = (req.getNickname() == null || req.getNickname().trim().isEmpty())
                ? req.getUsername() : req.getNickname();
        user.setNickname(nickname);
        userMapper.insert(user);
    }

    public LoginVO login(LoginRequest req) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", req.getUsername()));
        if (user == null || !encoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        LoginVO vo = new LoginVO();
        vo.setToken(jwtUtil.createToken(user.getId()));
        vo.setUserId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setBirthday(user.getBirthday() != null ? user.getBirthday().toString() : null);
        vo.setBg(user.getBg());
        vo.setSignature(user.getSignature());
        return vo;
    }

    /** 获取个人资料 */
    public UserProfileVO getProfile(Long userId) {
        User user = requireUser(userId);
        return toProfileVO(user);
    }

    /** 修改昵称 + 出生日期 + 个性签名 + 省市区 */
    public UserProfileVO updateProfile(Long userId, UpdateProfileRequest req) {
        User user = requireUser(userId);
        if (req.getNickname() != null && !req.getNickname().trim().isEmpty()) {
            user.setNickname(req.getNickname().trim());
        }
        if (req.getBirthday() != null) {
            String b = req.getBirthday().trim();
            if (b.isEmpty()) {
                user.setBirthday(null);
            } else {
                try {
                    user.setBirthday(LocalDate.parse(b));
                } catch (Exception e) {
                    throw new BusinessException("出生日期格式错误");
                }
            }
        }
        if (req.getSignature() != null) {
            user.setSignature(req.getSignature().trim().isEmpty() ? null : req.getSignature().trim());
        }
        if (req.getProvince() != null) {
            user.setProvince(req.getProvince().trim().isEmpty() ? null : req.getProvince().trim());
        }
        if (req.getCity() != null) {
            user.setCity(req.getCity().trim().isEmpty() ? null : req.getCity().trim());
        }
        if (req.getDistrict() != null) {
            user.setDistrict(req.getDistrict().trim().isEmpty() ? null : req.getDistrict().trim());
        }
        if (req.getGender() != null) {
            user.setGender(req.getGender().trim().isEmpty() ? null : req.getGender().trim());
        }
        if (req.getDailyGoal() != null) {
            if (req.getDailyGoal() <= 0 || req.getDailyGoal() > 1440) {
                throw new BusinessException("目标需在 1~1440 分钟之间");
            }
            user.setDailyGoal(req.getDailyGoal());
        }
        userMapper.updateById(user);
        return toProfileVO(user);
    }

    /** 修改密码（校验旧密码） */
    public void updatePassword(Long userId, UpdatePasswordRequest req) {
        User user = requireUser(userId);
        if (!encoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }
        user.setPassword(encoder.encode(req.getNewPassword()));
        userMapper.updateById(user);
    }

    /** 更新头像 */
    public UserProfileVO updateAvatar(Long userId, String avatarUrl) {
        User user = requireUser(userId);
        user.setAvatar(avatarUrl);
        userMapper.updateById(user);
        return toProfileVO(user);
    }

    /** 更新个人主页背景图 */
    public UserProfileVO updateBg(Long userId, String bgUrl) {
        User user = requireUser(userId);
        user.setBg(bgUrl);
        userMapper.updateById(user);
        return toProfileVO(user);
    }

    private User requireUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    private UserProfileVO toProfileVO(User user) {
        UserProfileVO vo = new UserProfileVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setBirthday(user.getBirthday() != null ? user.getBirthday().toString() : null);
        vo.setBg(user.getBg());
        vo.setSignature(user.getSignature());
        vo.setProvince(user.getProvince());
        vo.setCity(user.getCity());
        vo.setDistrict(user.getDistrict());
        vo.setGender(user.getGender());
        vo.setDailyGoal(user.getDailyGoal() == null ? 60 : user.getDailyGoal());
        return vo;
    }
}
