package com.riverflow.admin.infra.dynamicds;

import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * JDBC 驱动 JAR 文件校验器
 */
@Slf4j
@Component
public class JdbcDriverJarValidator {

    /**
     * 最大文件大小，默认 50MB
     */
    @Value("${riverflow.datasource.driver.max-file-size:52428800}")
    private long maxFileSize;

    /**
     * 校验上传的驱动 JAR 文件
     *
     * @param file        JAR 文件
     * @param driverClass 期望的驱动类名
     * @return 校验失败返回 R.fail，成功返回 null
     */
    public R<String> validate(MultipartFile file, String driverClass) {
        if (file == null || file.isEmpty()) {
            return R.fail("驱动 JAR 文件不能为空");
        }

        long fileSize = file.getSize();
        if (fileSize > maxFileSize) {
            return R.fail("驱动 JAR 文件大小不能超过 " + (maxFileSize / 1024 / 1024) + "MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".jar")) {
            return R.fail("只支持 JAR 格式的驱动文件");
        }

        // 防止路径遍历
        Path filenamePath = Paths.get(originalFilename).getFileName();
        if (filenamePath == null || !filenamePath.toString().equals(originalFilename)) {
            return R.fail("驱动 JAR 文件名不合法");
        }

        // 校验 JAR/ZIP 魔数 PK\x03\x04 或 PK\x05\x06
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[4];
            int read = is.read(header);
            if (read < 4) {
                return R.fail("驱动 JAR 文件内容不完整");
            }
            boolean isJar = header[0] == 'P' && header[1] == 'K'
                    && (header[2] == 0x03 || header[2] == 0x05)
                    && (header[3] == 0x04 || header[3] == 0x06);
            if (!isJar) {
                return R.fail("驱动 JAR 文件格式错误，仅支持合法的 JAR 文件");
            }
        } catch (IOException e) {
            log.error("读取驱动 JAR 文件失败", e);
            return R.fail("读取驱动 JAR 文件失败");
        }

        // 校验 JAR 中是否包含指定的驱动类
        if (driverClass != null && !driverClass.isEmpty()) {
            R<String> classCheck = checkDriverClass(file, driverClass);
            if (classCheck != null) {
                return classCheck;
            }
        }

        return null;
    }

    /**
     * 检查 JAR 中是否包含指定的驱动类
     */
    private R<String> checkDriverClass(MultipartFile file, String driverClass) {
        String classEntryName = driverClass.replace('.', '/') + ".class";
        try (JarInputStream jis = new JarInputStream(file.getInputStream())) {
            JarEntry entry;
            while ((entry = jis.getNextJarEntry()) != null) {
                if (entry.getName().equals(classEntryName)) {
                    return null;
                }
            }
            return R.fail("驱动 JAR 中未找到类: " + driverClass + "，请检查驱动类名是否正确");
        } catch (IOException e) {
            log.error("扫描驱动 JAR 失败", e);
            return R.fail("扫描驱动 JAR 失败");
        }
    }
}
