package com.testtracking.controller;

import com.testtracking.service.DatabaseBackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/backup")
@RequiredArgsConstructor
@Slf4j
public class DatabaseBackupController {

    private final DatabaseBackupService databaseBackupService;

    /**
     * 手动触发数据库备份
     */
    @PostMapping("/manual")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> manualBackup() {
        try {
            databaseBackupService.manualBackup();
            return ResponseEntity.ok("数据库备份成功");
        } catch (Exception e) {
            log.error("手动数据库备份失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("数据库备份失败: " + e.getMessage());
        }
    }

    /**
     * 获取备份文件列表
     */
    @GetMapping("/files")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getBackupFiles() {
        try {
            File[] files = databaseBackupService.getBackupFiles();
            List<BackupFileInfo> fileInfos = Arrays.stream(files)
                    .map(file -> new BackupFileInfo(
                            file.getName(),
                            file.length(),
                            file.lastModified()
                    ))
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(fileInfos);
        } catch (Exception e) {
            log.error("获取备份文件列表失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取备份文件列表失败");
        }
    }

    /**
     * 清理旧备份文件
     */
    @PostMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cleanupOldBackups() {
        try {
            databaseBackupService.cleanupOldBackups();
            return ResponseEntity.ok("清理旧备份文件成功");
        } catch (Exception e) {
            log.error("清理旧备份文件失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("清理旧备份文件失败: " + e.getMessage());
        }
    }

    /**
     * 备份文件信息
     */
    public static class BackupFileInfo {
        private String fileName;
        private long fileSize;
        private long lastModified;

        public BackupFileInfo(String fileName, long fileSize, long lastModified) {
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.lastModified = lastModified;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public long getFileSize() {
            return fileSize;
        }

        public void setFileSize(long fileSize) {
            this.fileSize = fileSize;
        }

        public long getLastModified() {
            return lastModified;
        }

        public void setLastModified(long lastModified) {
            this.lastModified = lastModified;
        }
    }
}
