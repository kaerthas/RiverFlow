package com.riverflow.admin.controller;

import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.spring.plugins.secondary.SecondaryVerificationApplication;
import com.riverflow.admin.infra.security.JwtUtil;
import com.riverflow.admin.infra.security.LoginUser;
import com.riverflow.admin.infra.security.PermissionService;
import com.riverflow.admin.infra.security.UserDetailsServiceImpl;
import com.riverflow.admin.service.LoginLockService;
import com.riverflow.admin.service.SysMenuService;
import com.riverflow.admin.service.SysUserService;
import com.riverflow.api.entity.SysMenu;
import com.riverflow.api.entity.SysUser;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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

    @Autowired
    private ImageCaptchaApplication imageCaptchaApplication;

    @Autowired
    private LoginLockService loginLockService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private SysMenuService sysMenuService;

    @Autowired
    private PermissionService permissionService;

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
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            return R.fail("用户名或密码不能为空");
        }

        String username = request.getUsername();
        log.info("用户登录: {}", username);

        // 1. 校验行为验证码（二次验证 token）
        String captchaToken = request.getCaptchaToken();
        if (captchaToken == null || captchaToken.isEmpty()) {
            return R.fail("请先完成验证码验证");
        }

        // 2. 检查账号是否被锁定（在验证码之后，避免刷验证码）
        long lockSeconds = loginLockService.checkLocked(username);
        if (lockSeconds > 0) {
            long minutes = (lockSeconds + 59) / 60;
            return R.fail("账号已被锁定，请 " + minutes + " 分钟后再试");
        }
        boolean captchaValid = false;
        try {
            captchaValid = ((SecondaryVerificationApplication) imageCaptchaApplication)
                    .secondaryVerification(captchaToken);
        } catch (Exception e) {
            log.warn("验证码二次验证异常: {}", e.getMessage());
        }
        if (!captchaValid) {
            return R.fail("验证码已失效，请重新验证");
        }

        // 3. 校验用户名密码
        SysUser user = ensureDefaultUser(username, request.getPassword());
        if (user == null) {
            recordLoginFailure(username);
            return R.fail("用户名或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            recordLoginFailure(username);
            return R.fail("用户名或密码错误");
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            return R.fail("账号已停用");
        }

        // 4. 登录成功，清除失败记录
        loginLockService.clearFailure(username);

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
     * 记录登录失败并给出提示
     */
    private void recordLoginFailure(String username) {
        int failCount = loginLockService.recordFailure(username);
        int remaining = loginLockService.getRemainingAttempts(username);
        if (remaining <= 0) {
            log.warn("用户 [{}] 连续登录失败 {} 次，账号已锁定", username, failCount);
        } else {
            log.warn("用户 [{}] 登录失败，已连续失败 {} 次，剩余 {} 次机会", username, failCount, remaining);
        }
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
     * 包含用户基本信息、角色、权限、菜单树
     */
    @GetMapping("/user/info")
    public R<Map<String, Object>> getUserInfo() {
        LoginUser loginUser = permissionService.getLoginUser();
        if (loginUser == null) {
            return R.fail("未登录");
        }

        Map<String, Object> user = new HashMap<>();
        user.put("userId", loginUser.getUserId());
        user.put("username", loginUser.getUsername());
        user.put("realName", loginUser.getRealName());
        user.put("avatar", loginUser.getAvatar());
        user.put("deptId", loginUser.getDeptId());
        user.put("deptName", loginUser.getDeptName());
        user.put("roles", loginUser.getRoles());
        user.put("permissions", loginUser.getPermissions());
        user.put("admin", loginUser.isAdmin());

        // 查询菜单树（仅目录和菜单，不含按钮权限）
        List<SysMenu> menus = sysMenuService.getMenusByUserId(loginUser.getUserId());
        List<SysMenu> menuTree = buildMenuTree(menus);
        user.put("menus", menuTree);

        return R.ok(user);
    }

    /**
     * 构建菜单树
     */
    private List<SysMenu> buildMenuTree(List<SysMenu> menus) {
        if (menus == null || menus.isEmpty()) {
            return new ArrayList<>();
        }
        // 过滤出目录和菜单类型，按排序号排序
        List<SysMenu> visibleMenus = menus.stream()
                .filter(m -> m.getMenuType() != null && m.getMenuType() <= 1)
                .sorted(Comparator.comparing(SysMenu::getSortNo, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(SysMenu::getId))
                .collect(Collectors.toList());

        Map<Long, SysMenu> menuMap = visibleMenus.stream().collect(Collectors.toMap(SysMenu::getId, m -> m));
        List<SysMenu> tree = new ArrayList<>();
        for (SysMenu menu : visibleMenus) {
            if (menu.getParentId() == null || menu.getParentId() == 0L) {
                tree.add(menu);
            } else {
                SysMenu parent = menuMap.get(menu.getParentId());
                if (parent != null) {
                    parent.getChildren().add(menu);
                }
            }
        }
        return tree;
    }

    /**
     * 登录请求
     */
    @lombok.Data
    public static class LoginRequest {
        private String username;
        private String password;
        private String captchaToken;
    }
}
