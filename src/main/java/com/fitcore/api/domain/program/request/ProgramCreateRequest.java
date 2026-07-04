package com.fitcore.api.domain.program.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ProgramCreateRequest {
    @NotBlank
    private String name;

    @NotEmpty
    @Size(min = 2, max = 20)
    private List<String> routineFinalIds;
}
