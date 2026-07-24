package com.streamflix.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchHistoryResponse {
    private Long id;
    private MovieResponse movie;
    private Integer progressMinutes;
    private boolean completed;
    private LocalDateTime lastWatchedAt;
}
