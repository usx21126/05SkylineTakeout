package com.sky.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Slf4j
//@Component
public class DemoTask {
    /**
     * 每隔5s触发
     */
    @Scheduled(cron = "*/5 * * * * *" )
    public void printLog(){
        log.info("【执行定时任务】");
    }
}
