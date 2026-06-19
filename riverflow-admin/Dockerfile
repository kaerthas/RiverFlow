# ==========================================
# Stage 1: Build
# ==========================================
FROM maven:3.9-eclipse-temurin-8-alpine AS builder

WORKDIR /build

# Copy pom files first to leverage Docker cache
COPY pom.xml .
COPY riverflow-common/pom.xml riverflow-common/
COPY riverflow-api/pom.xml riverflow-api/
COPY riverflow-admin/pom.xml riverflow-admin/
COPY river-minio/pom.xml river-minio/
COPY river-ixa-gateway/pom.xml river-ixa-gateway/
COPY river-qrcode-plugin/pom.xml river-qrcode-plugin/

# Download dependencies
RUN mvn dependency:go-offline -pl riverflow-admin -am -q

# Copy source code and build
COPY riverflow-common/src riverflow-common/src
COPY riverflow-api/src riverflow-api/src
COPY riverflow-admin/src riverflow-admin/src

RUN mvn clean package -pl riverflow-admin -am -DskipTests -q

# ==========================================
# Stage 2: Runtime
# ==========================================
FROM eclipse-temurin:8-jre-alpine

LABEL maintainer="RiverFlow Team"
LABEL description="RiverFlow Admin Service"

WORKDIR /app

# Install tzdata for timezone support
RUN apk add --no-cache tzdata \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

# Create directories for logs and plugins
RUN mkdir -p /app/logs /app/plugins

# Copy jar from builder
COPY --from=builder /build/riverflow-admin/target/riverflow-admin-1.0.0-SNAPSHOT.jar app.jar

ENV JAVA_OPTS="-Xms512m -Xmx1024m -Djava.security.egd=file:/dev/./urandom"
ENV TZ=Asia/Shanghai

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
