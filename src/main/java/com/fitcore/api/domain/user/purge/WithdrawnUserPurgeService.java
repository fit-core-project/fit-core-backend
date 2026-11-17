package com.fitcore.api.domain.user.purge;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 탈퇴 후 보관 기간(기본 30일)이 지난 사용자의 운동·식단·루틴 기록과 계정을 배치로 완전 삭제한다.
 * 사용자마다 별도 트랜잭션(WithdrawnUserDataPurger.purgeUser)으로 처리하고,
 * 로그에는 사용자 id 와 건수만 남긴다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawnUserPurgeService {

    private final WithdrawnUserDataPurger purger;

    @Value("${app.withdrawal-purge.retention-days:30}")
    private int retentionDays = 30;

    @Value("${app.withdrawal-purge.batch-size:100}")
    private int batchSize = 100;

    /**
     * now 기준 retentionDays 일 이전(경계 포함)에 탈퇴한 사용자를 모두 삭제한다.
     *
     * @return 삭제된 사용자 수
     */
    public int purgeExpired(LocalDateTime now) {
        LocalDateTime cutoff = now.minusDays(retentionDays);
        Set<String> failed = new HashSet<>();
        int purgedUsers = 0;

        while (true) {
            int limit = batchSize + failed.size();
            List<String> batch = purger.findExpiredUserIds(cutoff, limit);
            List<String> todo = batch.stream().filter(id -> !failed.contains(id)).toList();
            if (todo.isEmpty()) {
                break;
            }
            for (String userId : todo) {
                try {
                    Map<String, Integer> counts = purger.purgeUser(userId, cutoff);
                    if (!counts.isEmpty()) {
                        purgedUsers++;
                        log.info("[withdrawal-purge] purged userId={} counts={}", userId, counts);
                    }
                } catch (RuntimeException e) {
                    // 실패한 사용자는 롤백되고 이번 실행에서는 건너뛴다. 다음 실행에서 다시 시도한다.
                    failed.add(userId);
                    log.error("[withdrawal-purge] failed userId={} error={}",
                        userId, e.getClass().getSimpleName());
                }
            }
            if (batch.size() < limit) {
                break;
            }
        }

        log.info("[withdrawal-purge] done purgedUsers={} failedUsers={}", purgedUsers, failed.size());
        return purgedUsers;
    }
}
