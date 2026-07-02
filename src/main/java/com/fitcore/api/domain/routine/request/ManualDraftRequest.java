package com.fitcore.api.domain.routine.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ManualDraftRequest {

    @Size(max = 100)
    private String title;
}
