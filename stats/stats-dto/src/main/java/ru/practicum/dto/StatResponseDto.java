package ru.practicum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatResponseDto {

    @NotBlank(message = "App cannot be blank or null")
    private String app;

    @NotBlank(message = "URI cannot be blank or empty")
    private String uri;

    @NotNull
    private Long hits;
}
