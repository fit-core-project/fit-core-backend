package com.fitcore.api.domain.user.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.fitcore.api.domain.user.dto.BodyComposition;
import com.fitcore.api.domain.user.entity.UserProfileEntity;
import com.fitcore.api.domain.user.enums.UserStatus;

class UserProfileResponseTest {

    @Test
    void constructor_sortsSnapshotsNewestFirst_andKeepsUndatedSnapshotsLast() {
        UserProfileEntity user = UserProfileEntity.builder()
            .email("sort@fitcore.com")
            .status(UserStatus.ACTIVE)
            .build();
        List<BodyComposition> snapshots = new ArrayList<>();
        snapshots.add(BodyComposition.builder().measuredAt(LocalDate.of(2026, 1, 1)).bodyWeightKg(70.0).build());
        snapshots.add(BodyComposition.builder().measuredAt(null).bodyWeightKg(71.0).build());
        snapshots.add(BodyComposition.builder().measuredAt(LocalDate.of(2026, 6, 1)).bodyWeightKg(68.0).build());
        ReflectionTestUtils.setField(user, "bodyCompositionSnapshot", snapshots);

        UserProfileResponse response = new UserProfileResponse(user, List.of());

        assertThat(response.getBodyCompositionSnapshot())
            .extracting(BodyComposition::getBodyWeightKg)
            .containsExactly(68.0, 70.0, 71.0);
    }
}
