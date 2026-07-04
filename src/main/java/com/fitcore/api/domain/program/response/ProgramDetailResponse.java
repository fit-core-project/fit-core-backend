package com.fitcore.api.domain.program.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProgramDetailResponse {
    private String programId;
    private String name;
    private String status;
    private int currentPosition;
    private int totalItems;
    private LocalDateTime completedAt;
    private ProgramItemResponse nextItem;
    private List<ProgramItemResponse> items;
}
