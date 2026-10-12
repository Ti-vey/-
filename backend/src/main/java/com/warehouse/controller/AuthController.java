package com.warehouse.controller;

import com.warehouse.common.Result;
import com.warehouse.dto.LoginDTO;
import com.warehouse.security.LoginUser;
import com.warehouse.security.UserContext;
import com.warehouse.service.SysLogService;
import com.warehouse.service.SysUserService;
import com.warehouse.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 认证控制器：登录、获取当前用户、退出登录。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final SysUserService userService;
    private final SysLogService logService;

    /** 用户登录 */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        long start = System.currentTimeMillis();
        LoginVO loginVO = userService.login(dto.getUsername(), dto.getPassword());
        logService.record(dto.getUsername(), "认证管理", "用户登录", "POST /api/auth/login",
                dto.getUsername(), System.currentTimeMillis() - start);
        return Result.success("登录成功", loginVO);
    }

    /** 获取当前登录用户信息 */
    @GetMapping("/me")
    public Result<LoginUser> me() {
        return Result.success(UserContext.get());
    }

    /** 退出登录：采用 JWT 无状态认证，前端清除令牌即可 */
    @PostMapping("/logout")
    public Result<Void> logout() {
        LoginUser user = UserContext.get();
        if (user != null) {
            logService.record(user.getUsername(), "认证管理", "退出登录", "POST /api/auth/logout", null, 0L);
        }
        return Result.success();
    }
}
