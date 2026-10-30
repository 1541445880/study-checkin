# =============================================================
# 学习打卡系统 - 后端 Spring Boot 镜像
# 构建：docker build -t study-checkin-backend .
# =============================================================
FROM openjdk:11-jre-slim

WORKDIR /app

# 复制打包好的 jar（先在本机执行 mvn clean package -DskipTests）
COPY target/study-checkin-backend-1.0.0.jar app.jar

EXPOSE 8080

# 说明：
# 1) -Djava.net.preferIPv4Stack=true 强制走 IPv4，规避服务器 IPv6 路由不通导致的豆包连接超时
# 2) 数据库连接地址通过 docker-compose 的环境变量 SPRING_DATASOURCE_URL 覆盖为 mysql:3306
ENTRYPOINT ["java", "-Djava.net.preferIPv4Stack=true", "-jar", "app.jar"]
