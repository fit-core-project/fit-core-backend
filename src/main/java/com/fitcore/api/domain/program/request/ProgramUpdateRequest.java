package com.fitcore.api.domain.program.request;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@Setter
public class ProgramUpdateRequest {
    @NotBlank
    @Size(max = 255)
    private String name;
}
