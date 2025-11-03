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

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 使用国内镜像源（解决DNS和加速apt-get）
RUN sed -i 's/deb.debian.org/mirrors.aliyun.com/g' /etc/apt/sources.list && \
    sed -i 's/security.debian.org/mirrors.aliyun.com/g' /etc/apt/sources.list

# 安装mysqldump工具（数据库备份功能需要）
RUN apt-get update && apt-get install -y default-mysql-client && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# 从编译阶段复制JAR文件
COPY --from=build /app/target/test-task-tracking-1.0.0.jar app.jar

# 创建日志目录和备份目录
RUN mkdir -p /app/logs /backup

# 暴露端口
EXPOSE 8080

# 设置JVM参数
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 