@echo off
chcp 65001 >nul
echo =========================================
echo   RiverFlow MinIO插件编译脚本
echo =========================================

cd river-minio

echo.
echo 1. 清理旧文件...
call mvn clean

echo.
echo 2. 编译打包...
call mvn package -DskipTests

echo.
echo 3. 检查编译结果...
if exist "target\river-minio-1.0.0-SNAPSHOT.jar" (
    echo ✅ 编译成功！
    echo.
    echo JAR包位置: target\river-minio-1.0.0-SNAPSHOT.jar
    for %%A in (target\river-minio-1.0.0-SNAPSHOT.jar) do echo 文件大小: %%~zA 字节
    echo.
    echo 部署方法：
    echo   copy target\river-minio-1.0.0-SNAPSHOT.jar ..\plugins\
) else (
    echo ❌ 编译失败！
    pause
    exit /b 1
)

echo.
echo =========================================
pause
