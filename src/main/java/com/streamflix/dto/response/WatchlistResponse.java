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
public class WatchlistResponse {
    private Long id;
    private MovieResponse movie;
    private LocalDateTime addedAt;
}
