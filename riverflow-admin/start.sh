#!/bin/bash
# ============================================
# RiverFlow Admin 启动脚本
# 放置位置：与 river-admin.jar 同级目录
# ============================================

APP_NAME="riverflow-admin"
JAR_NAME="riverflow-admin-1.0.0-SNAPSHOT.jar"
LOG_PATH="./logs"

# 检查 jar 文件是否存在
if [ ! -f "${JAR_NAME}" ]; then
    echo "错误: 未找到 ${JAR_NAME}，请确认脚本与 jar 包处于同级目录"
    exit 1
fi

# 创建日志目录
mkdir -p ${LOG_PATH}

# 检查进程是否已启动
PID=$(ps -ef | grep "${JAR_NAME}" | grep -v grep | awk '{print $2}')
if [ -n "$PID" ]; then
    echo "警告: ${APP_NAME} 已在运行，PID=${PID}"
    exit 1
fi

# 启动应用
# 标准输出/错误重定向到 /dev/null，业务日志由 logback 写入 ./logs/
nohup java -jar ${JAR_NAME} \
    --logging.file.path=${LOG_PATH} \
    > /dev/null 2>&1 &

# 记录 PID
echo $! > ${APP_NAME}.pid
echo "${APP_NAME} 启动成功，PID=$!"
echo "日志目录: ${LOG_PATH}/"
