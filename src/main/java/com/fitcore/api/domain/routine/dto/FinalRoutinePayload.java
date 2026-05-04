package com.fitcore.api.domain.routine.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

import com.fitcore.api.infrastructure.ai.enums.GenerationStatus;
import com.fitcore.api.infrastructure.ai.enums.StatusReasonCode;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class FinalRoutinePayload {
    private boolean fallback;
    private GenerationStatus generationStatus;
    private List<String> rationaleSummary;
    private List<RoutineBlock> routineBlocks;
    private StatusReasonCode statusReasonCode;
    private String summaryTitle;
    private int totalEstimatedTime;
    private List<String> warnings;
}
