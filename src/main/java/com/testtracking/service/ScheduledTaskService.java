package com.testtracking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledTaskService {

    private final LoginHistoryService loginHistoryService;

    /**
     * 每天凌晨2点清理一个月前的登录历史记录
     * cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanOldLoginHistory() {
        try {
            log.info("开始执行登录历史清理任务");
            loginHistoryService.cleanOldLoginHistory();
            log.info("登录历史清理任务执行完成");
        } catch (Exception e) {
            log.error("登录历史清理任务执行失败: {}", e.getMessage(), e);
        }
    }
}
