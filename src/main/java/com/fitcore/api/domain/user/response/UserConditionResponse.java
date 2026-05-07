package com.fitcore.api.domain.user.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
        this.doms = Optional.ofNullable(entity.getDoms())
            .orElseGet(Collections::emptyList);
    }
}
