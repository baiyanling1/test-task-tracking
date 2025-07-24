# 多阶段构建 - 编译阶段
FROM maven:3.8.4-openjdk-11 AS build

WORKDIR /app

# 复制pom.xml
COPY pom.xml .

# 下载依赖
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 编译项目
RUN mvn clean package -DskipTests

# 运行阶段
FROM openjdk:11-jre-slim

WORKDIR /app

# 从编译阶段复制JAR文件
COPY --from=build /app/target/test-task-tracking-1.0.0.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs

# 暴露端口
EXPOSE 8080

# 设置JVM参数
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 