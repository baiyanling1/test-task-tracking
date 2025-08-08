package com.testtracking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseBackupService {

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${backup.path:/backup}")
    private String backupPath;

    @Value("${backup.retention.days:30}")
    private int retentionDays;

    /**
     * 每周一凌晨2点执行数据库备份
     */
    @Scheduled(cron = "0 0 2 * * 1")
    public void scheduledBackup() {
        log.info("开始执行定时数据库备份...");
        try {
            performBackup();
            cleanupOldBackups();
            log.info("定时数据库备份完成");
        } catch (Exception e) {
            log.error("定时数据库备份失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 执行数据库备份
     */
    public void performBackup() {
        try {
            // 创建备份目录
            Path backupDir = Paths.get(backupPath);
            if (!Files.exists(backupDir)) {
                Files.createDirectories(backupDir);
            }

            // 生成备份文件名
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFileName = String.format("test_tracking_backup_%s.sql", timestamp);
            Path backupFile = backupDir.resolve(backupFileName);

            // 构建mysqldump命令
            String[] command = buildMysqldumpCommand(backupFile.toString());

            // 执行备份命令
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("数据库备份成功: {}", backupFile);
            } else {
                log.error("数据库备份失败，退出码: {}", exitCode);
                throw new RuntimeException("数据库备份失败");
            }

        } catch (Exception e) {
            log.error("执行数据库备份时发生错误: {}", e.getMessage(), e);
            throw new RuntimeException("数据库备份失败", e);
        }
    }

    /**
     * 构建mysqldump命令
     */
    private String[] buildMysqldumpCommand(String backupFile) {
        // 从JDBC URL中提取数据库信息
        String dbName = extractDatabaseName(dbUrl);
        String host = extractHost(dbUrl);
        String port = extractPort(dbUrl);

        return new String[]{
                "mysqldump",
                "-h", host,
                "-P", port,
                "-u", dbUsername,
                "-p" + dbPassword,
                "--single-transaction",
                "--routines",
                "--triggers",
                "--add-drop-database",
                "--create-options",
                dbName
        };
    }

    /**
     * 从JDBC URL中提取数据库名
     */
    private String extractDatabaseName(String jdbcUrl) {
        // 示例: jdbc:mysql://localhost:3306/test_tracking
        int lastSlashIndex = jdbcUrl.lastIndexOf('/');
        int questionMarkIndex = jdbcUrl.indexOf('?');
        
        if (lastSlashIndex != -1) {
            String dbPart = questionMarkIndex != -1 ? 
                jdbcUrl.substring(lastSlashIndex + 1, questionMarkIndex) : 
                jdbcUrl.substring(lastSlashIndex + 1);
            return dbPart;
        }
        
        return "test_tracking"; // 默认数据库名
    }

    /**
     * 从JDBC URL中提取主机名
     */
    private String extractHost(String jdbcUrl) {
        // 示例: jdbc:mysql://localhost:3306/test_tracking
        String urlPart = jdbcUrl.replace("jdbc:mysql://", "");
        int colonIndex = urlPart.indexOf(':');
        int slashIndex = urlPart.indexOf('/');
        
        if (colonIndex != -1 && slashIndex != -1) {
            return urlPart.substring(0, colonIndex);
        }
        
        return "localhost"; // 默认主机名
    }

    /**
     * 从JDBC URL中提取端口号
     */
    private String extractPort(String jdbcUrl) {
        // 示例: jdbc:mysql://localhost:3306/test_tracking
        String urlPart = jdbcUrl.replace("jdbc:mysql://", "");
        int colonIndex = urlPart.indexOf(':');
        int slashIndex = urlPart.indexOf('/');
        
        if (colonIndex != -1 && slashIndex != -1) {
            return urlPart.substring(colonIndex + 1, slashIndex);
        }
        
        return "3306"; // 默认端口号
    }

    /**
     * 清理旧的备份文件
     */
    public void cleanupOldBackups() {
        try {
            Path backupDir = Paths.get(backupPath);
            if (!Files.exists(backupDir)) {
                return;
            }

            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);

            try (Stream<Path> files = Files.list(backupDir)) {
                files.filter(path -> path.toString().endsWith(".sql"))
                     .filter(path -> {
                         try {
                             String fileName = path.getFileName().toString();
                             // 从文件名中提取时间戳
                             if (fileName.contains("_")) {
                                 String timestampStr = fileName.split("_")[1].replace(".sql", "");
                                 LocalDateTime fileDate = LocalDateTime.parse(timestampStr, 
                                     DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                                 return fileDate.isBefore(cutoffDate);
                             }
                             return false;
                         } catch (Exception e) {
                             log.warn("无法解析文件时间戳: {}", path);
                             return false;
                         }
                     })
                     .forEach(path -> {
                         try {
                             Files.delete(path);
                             log.info("删除旧备份文件: {}", path);
                         } catch (IOException e) {
                             log.error("删除旧备份文件失败: {}", path, e);
                         }
                     });
            }

        } catch (Exception e) {
            log.error("清理旧备份文件时发生错误: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取备份文件列表
     */
    public File[] getBackupFiles() {
        try {
            Path backupDir = Paths.get(backupPath);
            if (!Files.exists(backupDir)) {
                return new File[0];
            }

            File[] files = backupDir.toFile().listFiles((dir, name) -> name.endsWith(".sql"));
            if (files != null) {
                Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
            }
            return files != null ? files : new File[0];

        } catch (Exception e) {
            log.error("获取备份文件列表时发生错误: {}", e.getMessage(), e);
            return new File[0];
        }
    }

    /**
     * 手动触发备份
     */
    public void manualBackup() {
        log.info("开始执行手动数据库备份...");
        try {
            performBackup();
            log.info("手动数据库备份完成");
        } catch (Exception e) {
            log.error("手动数据库备份失败: {}", e.getMessage(), e);
            throw e;
        }
    }
}
