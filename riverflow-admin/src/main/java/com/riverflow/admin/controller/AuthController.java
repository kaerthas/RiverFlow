package com.riverflow.admin.controller;

import com.riverflow.admin.infra.security.JwtUtil;
import com.riverflow.admin.service.SysUserService;
import com.riverflow.api.entity.SysUser;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 认证 Controller
 */
@Slf4j
@RestController
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 初始化默认管理员账户（仅开发测试用）
     */
    private SysUser ensureDefaultUser(String username, String rawPassword) {
        SysUser user = sysUserService.getByUsername(username);
        if (user == null && "admin".equals(username)) {
            user = new SysUser();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRealName("系统管理员");
            user.setStatus(1);
            sysUserService.save(user);
        }
        return user;
    }

    // 简单的 Refresh Token 黑名单（生产环境应使用 Redis）
    private final Map<String, Boolean> refreshTokenBlacklist = new ConcurrentHashMap<>();

    /**
     * 登录
     */
    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody LoginRequest request) {
        log.info("用户登录: {}", request.getUsername());

        SysUser user = ensureDefaultUser(request.getUsername(), request.getPassword());
        if (user == null) {
            return R.fail("用户名或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return R.fail("用户名或密码错误");
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            return R.fail("账号已停用");
        }

        String accessToken = jwtUtil.generateToken(user.getUsername(), user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", accessToken);
        result.put("refreshToken", refreshToken);
        result.put("tokenType", "Bearer");
        result.put("expiresIn", 86400);
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());

        return R.ok(result);
    }

    /**
     * 刷新 Token
     */
    @PostMapping("/refresh")
    public R<Map<String, Object>> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isEmpty()) {
            return R.fail("Refresh Token 不能为空");
        }

        if (refreshTokenBlacklist.containsKey(refreshToken)) {
            return R.fail("Refresh Token 已失效");
        }

        if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            return R.fail("Refresh Token 无效");
        }

        String username = jwtUtil.getUsernameFromToken(refreshToken);
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);

        SysUser user = sysUserService.getByUsername(username);
        if (user == null || user.getStatus() == 0) {
            return R.fail("用户不存在或已停用");
        }

        String newAccessToken = jwtUtil.generateToken(username, userId);
        String newRefreshToken = jwtUtil.generateRefreshToken(username, userId);

        // 旧 Refresh Token 加入黑名单
        refreshTokenBlacklist.put(refreshToken, true);

        Map<String, Object> result = new HashMap<>();
        result.put("token", newAccessToken);
        result.put("refreshToken", newRefreshToken);
        result.put("tokenType", "Bearer");
        result.put("expiresIn", 86400);

        return R.ok(result);
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public R<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // 将 token 加入黑名单（生产环境应使用 Redis）
            refreshTokenBlacklist.put(token, true);
        }
        return R.ok();
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/user/info")
    public R<Map<String, Object>> getUserInfo() {
        Map<String, Object> user = new HashMap<>();
        user.put("username", "admin");
        user.put("realName", "系统管理员");
        user.put("avatar", "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png");
        user.put("roles", new String[]{"admin"});
        return R.ok(user);
    }

    /**
     * 登录请求
     */
    @lombok.Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
}
