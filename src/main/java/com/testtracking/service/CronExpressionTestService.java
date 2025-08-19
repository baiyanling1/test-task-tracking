package com.testtracking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Service
public class CronExpressionTestService {

    /**
     * 测试cron表达式解析
     */
    public void testCronExpressions() {
        String[] cronExpressions = {
            "0 0 1 * * ?",      // 每天凌晨1点
            "0 0 2 * * ?",      // 每天凌晨2点
            "0 0 2 * * MON",    // 每周一凌晨2点
            "0 30 9 * * MON"    // 每周一早上9点半
        };

        for (String cron : cronExpressions) {
            try {
                log.info("测试cron表达式: {}", cron);
                CronTrigger trigger = new CronTrigger(cron);
                Date now = new Date();
                Date nextExecution = trigger.nextExecutionTime(new SimpleTriggerContext(now, now, now));
                if (nextExecution != null) {
                    LocalDateTime nextTime = LocalDateTime.ofInstant(nextExecution.toInstant(), ZoneId.of("Asia/Shanghai"));
                    log.info("下次执行时间: {}", nextTime);
                } else {
                    log.warn("无法计算下次执行时间");
                }
            } catch (Exception e) {
                log.error("cron表达式解析失败: {}, error: {}", cron, e.getMessage());
            }
        }
    }
}
