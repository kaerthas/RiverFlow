#!/bin/bash

echo "========================================="
echo "  RiverFlow MinIO插件编译脚本"
echo "========================================="

cd river-minio

echo ""
echo "1. 清理旧文件..."
mvn clean

echo ""
echo "2. 编译打包..."
mvn package -DskipTests

echo ""
echo "3. 检查编译结果..."
if [ -f "target/river-minio-1.0.0-SNAPSHOT.jar" ]; then
    echo "✅ 编译成功！"
    echo ""
    echo "JAR包位置: target/river-minio-1.0.0-SNAPSHOT.jar"
    echo "文件大小: $(ls -lh target/river-minio-1.0.0-SNAPSHOT.jar | awk '{print $5}')"
    echo ""
    echo "部署方法："
    echo "  cp target/river-minio-1.0.0-SNAPSHOT.jar ../plugins/"
else
    echo "❌ 编译失败！"
    exit 1
fi

echo ""
echo "========================================="
