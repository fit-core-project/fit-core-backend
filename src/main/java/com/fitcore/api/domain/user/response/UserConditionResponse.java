package com.fitcore.api.domain.user.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.fitcore.api.domain.routine.dto.Doms;
import com.fitcore.api.domain.user.dto.PainAreas;
import com.fitcore.api.domain.user.entity.UserProfileEntity;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class UserConditionResponse {
    private List<String> painAreas;
    private List<Doms> doms;

    public UserConditionResponse(UserProfileEntity entity) {
        this.painAreas = Optional.ofNullable(entity.getPainAreas())
            .orElseGet(Collections::emptyList)
            .stream()
            .map(PainAreas::getArea)
            .toList();
        this.doms = applyDecay(Optional.ofNullable(entity.getDoms()).orElseGet(Collections::emptyList));
    }

    private static List<Doms> applyDecay(List<Doms> stored) {
        LocalDate today = LocalDate.now();
        return stored.stream()
            .filter(d -> d.getRecordedAt() != null)
            .map(d -> {
                long days = ChronoUnit.DAYS.between(d.getRecordedAt(), today);
                if (days >= 2) return null;
                String effectiveLevel = days == 1 ? "mild" : "moderate";
                return Doms.builder()
                    .bodyPart(d.getBodyPart())
                    .level(effectiveLevel)
                    .recordedAt(d.getRecordedAt())
                    .build();
            })
            .filter(d -> d != null)
            .toList();
    }
}
