package com.fitcore.api.domain.user.purge;

import java.time.LocalDateTime;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 매일 04:00 (Asia/Seoul) 탈퇴 사용자 데이터를 정리한다. app.withdrawal-purge.enabled=false 로 끈다.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.withdrawal-purge.enabled", havingValue = "true")
public class WithdrawnUserPurgeScheduler {

    private final WithdrawnUserPurgeService purgeService;

    @Scheduled(cron = "${app.withdrawal-purge.cron:0 0 4 * * *}", zone = "Asia/Seoul")
    public void run() {
        // deleted_at 은 JVM 기본 시간대의 LocalDateTime.now() 로 기록되므로 같은 기준으로 비교한다.
        purgeService.purgeExpired(LocalDateTime.now());
    }
}
