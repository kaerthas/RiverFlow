package com.riverflow.admin.infra.plugin;

import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 插件文件校验器
 * <p>
 * 统一校验上传的插件文件格式、大小、文件名安全性等。
 * 供 {@link PluginController}、{@link NodePluginLoader} 等入口复用。
 */
@Slf4j
@Component
public class PluginFileValidator {

    @Value("${riverflow.plugin.max-file-size:52428800}")
    private long maxFileSize;

    /**
     * 校验插件文件是否合法
     *
     * @param file 上传文件
     * @return 校验通过返回 null，否则返回错误响应
     */
    public R<String> validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return R.fail("插件文件不能为空");
        }

        long fileSize = file.getSize();
        if (fileSize > maxFileSize) {
            return R.fail("插件文件大小不能超过 " + (maxFileSize / 1024 / 1024) + "MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".jar")) {
            return R.fail("只支持 JAR 格式插件");
        }

        // 防止路径遍历
        Path filenamePath = Paths.get(originalFilename).getFileName();
        if (filenamePath == null) {
            return R.fail("插件文件名不合法");
        }
        String safeName = filenamePath.toString();
        if (!safeName.equals(originalFilename)) {
            return R.fail("插件文件名不合法");
        }

        // 校验文件魔数：JAR 本质是 ZIP，文件头为 PK\x03\x04 或 PK\x05\x06
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[4];
            int read = is.read(header);
            if (read < 4) {
                return R.fail("插件文件内容不完整");
            }
            boolean isJar = header[0] == 'P' && header[1] == 'K'
                    && (header[2] == 0x03 || header[2] == 0x05)
                    && (header[3] == 0x04 || header[3] == 0x06);
            if (!isJar) {
                return R.fail("插件文件格式错误，仅支持合法的 JAR 文件");
            }
        } catch (IOException e) {
            log.warn("读取插件文件头失败", e);
            return R.fail("插件文件读取失败");
        }

        return null;
    }
}
