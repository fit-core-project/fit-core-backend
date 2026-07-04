package com.fitcore.api.domain.program.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProgramItemResponse {
    private String programItemId;
    private int position;
    private String routineFinalId;
    private String title;
    private String targetSplitLabel;
    private Integer estimatedTime;
    private String state;
}
