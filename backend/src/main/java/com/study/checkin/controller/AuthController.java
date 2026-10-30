package com.study.checkin.controller;

import com.study.checkin.common.Result;
import com.study.checkin.dto.LoginRequest;
import com.study.checkin.dto.RegisterRequest;
import com.study.checkin.service.UserService;
import com.study.checkin.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterRequest req) {
        userService.register(req);
        return Result.ok();
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest req) {
        return Result.ok(userService.login(req));
    }
}
