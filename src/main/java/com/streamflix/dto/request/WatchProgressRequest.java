package com.streamflix.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatchProgressRequest {

    @NotNull
    private Long movieId;

    @NotNull
    @Min(0)
    private Integer progressMinutes;

    private Boolean completed;
}
