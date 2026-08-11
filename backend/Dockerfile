# ==============================================================
# 大健康智能问诊系统 - Java 后端镜像
# ==============================================================
# 构建阶段
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build
COPY pom.xml .
# 提前下载依赖，充分利用构建缓存
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# 运行阶段
FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=builder /build/target/health-diagnosis-backend-*.jar app.jar

EXPOSE 8080

ENV TZ=Asia/Shanghai

ENTRYPOINT ["java", "-jar", "app.jar"]
