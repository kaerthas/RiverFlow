package com.riverflow.admin.controller;

import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.application.vo.ImageCaptchaVO;
import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.common.response.ApiResponse;
import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import com.riverflow.common.result.R;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 行为验证码 Controller
 */
@Slf4j
@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    @Autowired
    private ImageCaptchaApplication imageCaptchaApplication;

    /**
     * 生成验证码
     * <p>
     * 返回结构需符合 tianai-captcha-web-sdk 约定：{code:200, data:{...}}
     */
    @PostMapping("/generate")
    public R<ImageCaptchaVO> generate() {
        ApiResponse<ImageCaptchaVO> response = imageCaptchaApplication.generateCaptcha(CaptchaTypeConstant.SLIDER);
        if (!response.isSuccess()) {
            log.warn("生成验证码失败: {}", response.getMsg());
            return R.fail(response.getCode(), response.getMsg());
        }
        return R.ok(response.getData());
    }

    /**
     * 校验验证码
     * <p>
     * 返回结构需符合 tianai-captcha-web-sdk 约定：{code:200, data:{id:'xxx'}} 或 {code:500, msg:'xxx'}
     */
    @PostMapping("/check")
    public R<Map<String, String>> check(@RequestBody CaptchaCheckRequest request) {
        if (request == null || request.getId() == null || request.getData() == null) {
            return R.fail("验证码参数不能为空");
        }

        ApiResponse<?> response = imageCaptchaApplication.matching(request.getId(), request.getData());
        if (!response.isSuccess()) {
            log.debug("验证码校验失败: {}", response.getMsg());
            return R.fail(response.getCode(), response.getMsg());
        }

        // 二次验证 token，前端拿到后需随登录请求回传
        Map<String, String> data = new HashMap<>();
        data.put("id", request.getId());
        return R.ok(data);
    }

    /**
     * 验证码校验请求
     */
    @Data
    public static class CaptchaCheckRequest {
        private String id;
        private ImageCaptchaTrack data;
    }
}
