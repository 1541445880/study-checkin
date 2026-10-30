package com.study.checkin.controller;

import com.study.checkin.common.Result;
import com.study.checkin.dto.AvatarRequest;
import com.study.checkin.dto.BgRequest;
import com.study.checkin.dto.UpdatePasswordRequest;
import com.study.checkin.dto.UpdateProfileRequest;
import com.study.checkin.service.UserService;
import com.study.checkin.vo.UserProfileVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public Result<UserProfileVO> profile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    public Result<UserProfileVO> updateProfile(@RequestBody UpdateProfileRequest req, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(userService.updateProfile(userId, req));
    }

    @PutMapping("/password")
    public Result<?> updatePassword(@Valid @RequestBody UpdatePasswordRequest req, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userService.updatePassword(userId, req);
        return Result.ok();
    }

    @PutMapping("/avatar")
    public Result<UserProfileVO> updateAvatar(@Valid @RequestBody AvatarRequest req, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(userService.updateAvatar(userId, req.getAvatarUrl()));
    }

    @PutMapping("/bg")
    public Result<UserProfileVO> updateBg(@Valid @RequestBody BgRequest req, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(userService.updateBg(userId, req.getUrl()));
    }
}
