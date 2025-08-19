package com.testtracking.service;

import com.testtracking.entity.ScheduledTask;
import com.testtracking.repository.ScheduledTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseBackupService {

    private final ScheduledTaskRepository scheduledTaskRepository;

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
    @Scheduled(cron = "0 0 2 * * MON")
    public void scheduledBackup() {
        log.info("开始执行定时数据库备份...");
        
        // 使用Asia/Shanghai时区的当前时间
        LocalDateTime executeTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        
        try {
            performBackup();
            cleanupOldBackups();
            log.info("定时数据库备份完成");
            
            // 更新定时任务执行记录
            updateScheduledTaskExecution("scheduledBackup", executeTime, "SUCCESS", null);
            
        } catch (Exception e) {
            log.error("定时数据库备份失败: {}", e.getMessage(), e);
            updateScheduledTaskExecution("scheduledBackup", executeTime, "FAILED", e.getMessage());
            throw e;
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

            log.info("开始数据库备份: {}", backupFile);

            // 构建mysqldump命令
            String[] command = buildMysqldumpCommand(backupFile.toString());

            // 执行备份命令
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);

            log.info("执行mysqldump命令: {}", String.join(" ", command));

            Process process = processBuilder.start();
            
            // 读取输出流
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("mysqldump输出: {}", line);
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("数据库备份成功: {}", backupFile);
            } else {
                log.error("数据库备份失败，退出码: {}, 输出: {}", exitCode, output.toString());
                throw new RuntimeException("数据库备份失败，退出码: " + exitCode + ", 错误信息: " + output.toString());
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
        // 直接使用固定的数据库名，避免解析问题
        String dbName = "test_tracking";
        String host = "mysql";
        String port = "3306";

        log.info("构建mysqldump命令: host={}, port={}, dbName={}, backupFile={}", host, port, dbName, backupFile);

        return new String[]{
                "mysqldump",
                "-h", host,
                "-P", port,
                "-u", "root",
                "-p" + "TestTracking@2024",
                "--single-transaction",
                "--routines",
                "--triggers",
                "--add-drop-database",
                "--create-options",
                "--result-file=" + backupFile,
                dbName
        };
    }

    /**
     * 从JDBC URL中提取数据库名
     */
    private String extractDatabaseName(String jdbcUrl) {
        try {
            // 示例: jdbc:mysql://localhost:3306/test_tracking?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
            String urlPart = jdbcUrl.replace("jdbc:mysql://", "");
            
            // 找到最后一个斜杠后的部分
            int lastSlashIndex = urlPart.lastIndexOf('/');
            if (lastSlashIndex == -1) {
                return "test_tracking"; // 默认数据库名
            }
            
            String dbPart = urlPart.substring(lastSlashIndex + 1);
            
            // 移除查询参数
            int questionMarkIndex = dbPart.indexOf('?');
            if (questionMarkIndex != -1) {
                dbPart = dbPart.substring(0, questionMarkIndex);
            }
            
            log.info("从JDBC URL提取数据库名: {} -> {}", jdbcUrl, dbPart);
            return dbPart;
        } catch (Exception e) {
            log.error("提取数据库名失败，JDBC URL: {}, 错误: {}", jdbcUrl, e.getMessage());
            return "test_tracking"; // 默认数据库名
        }
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

    /**
     * 更新定时任务执行记录
     */
    private void updateScheduledTaskExecution(String taskName, LocalDateTime executeTime, String result, String errorMessage) {
        try {
            ScheduledTask task = scheduledTaskRepository.findByTaskName(taskName)
                .orElseThrow(() -> new RuntimeException("任务不存在: " + taskName));
            
            task.setLastExecuteTime(executeTime);
            task.setLastExecuteResult(result);
            
            // 自动执行时更新下次执行时间
            LocalDateTime nextExecuteTime = calculateNextExecuteTime(task.getCronExpression());
            task.setNextExecuteTime(nextExecuteTime);
            
            scheduledTaskRepository.save(task);
            
            log.info("定时任务 {} 执行记录已更新: 时间={}, 结果={}, 下次执行时间={}", taskName, executeTime, result, nextExecuteTime);
        } catch (Exception e) {
            log.error("更新定时任务执行记录失败: taskName={}, error={}", taskName, e.getMessage());
        }
    }

    /**
     * 计算下次执行时间
     */
    private LocalDateTime calculateNextExecuteTime(String cronExpression) {
        try {
            CronTrigger trigger = new CronTrigger(cronExpression);
            Date now = new Date();
            Date nextExecution = trigger.nextExecutionTime(new SimpleTriggerContext(now, now, now));
            if (nextExecution != null) {
                return LocalDateTime.ofInstant(nextExecution.toInstant(), ZoneId.of("Asia/Shanghai"));
            }
        } catch (Exception e) {
            log.error("解析cron表达式失败: {}, error: {}", cronExpression, e.getMessage());
        }
        return null;
    }
}
