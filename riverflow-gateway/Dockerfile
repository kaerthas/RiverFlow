# ==========================================
# Stage 1: Build
# ==========================================
FROM maven:3.9-eclipse-temurin-8-alpine AS builder

WORKDIR /build

# Copy pom files first to leverage Docker cache
# （根 pom 的 modules 聚合了解析所需的全部子模块 pom，缺一不可）
COPY pom.xml .
COPY riverflow-common/pom.xml riverflow-common/
COPY riverflow-api/pom.xml riverflow-api/
COPY riverflow-admin/pom.xml riverflow-admin/
COPY riverflow-gateway/pom.xml riverflow-gateway/
COPY river-minio/pom.xml river-minio/
COPY river-petsapi/pom.xml river-petsapi/
COPY river-ixa-gateway/pom.xml river-ixa-gateway/
COPY river-qrcode-plugin/pom.xml river-qrcode-plugin/
COPY river-apisix-auth/pom.xml river-apisix-auth/
COPY river-hw-auth/pom.xml river-hw-auth/

# Download dependencies
RUN mvn dependency:go-offline -pl riverflow-gateway -am -q

# Copy source code and build（gateway 仅依赖 riverflow-common）
COPY riverflow-common/src riverflow-common/src
COPY riverflow-gateway/src riverflow-gateway/src

RUN mvn clean package -pl riverflow-gateway -am -DskipTests -q

# ==========================================
# Stage 2: Runtime
# ==========================================
FROM eclipse-temurin:8-jre-alpine

LABEL maintainer="RiverFlow Team"
LABEL description="RiverFlow API Gateway"

WORKDIR /app

# Install tzdata for timezone support
RUN apk add --no-cache tzdata \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

RUN mkdir -p /app/logs

# Copy jar from builder
COPY --from=builder /build/riverflow-gateway/target/riverflow-gateway-1.0.0-SNAPSHOT.jar app.jar

ENV JAVA_OPTS="-Xms256m -Xmx512m -Djava.security.egd=file:/dev/./urandom"
ENV TZ=Asia/Shanghai

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
