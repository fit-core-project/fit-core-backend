package com.fitcore.api.domain.program.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProgramSummaryResponse {
    private String programId;
    private String name;
    private String status;
    private int currentPosition;
    private int totalItems;
    private LocalDateTime completedAt;
    private ProgramItemResponse nextItem;
}
