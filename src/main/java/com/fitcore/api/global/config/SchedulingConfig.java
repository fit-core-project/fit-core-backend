package com.fitcore.api.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 스케줄 작업 활성화. 개별 작업은 각자의 프로퍼티로 켜고 끈다
 * (예: app.withdrawal-purge.enabled).
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
