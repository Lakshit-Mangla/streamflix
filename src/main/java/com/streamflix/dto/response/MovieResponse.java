package com.streamflix.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieResponse {
    private Long id;
    private String title;
    private String description;
    private Integer releaseYear;
    private Integer durationMinutes;
    private String posterUrl;
    private String videoUrl;
    private String contentType;
    private Double averageRating;
    private Long viewCount;
    private Set<String> genres;
    private String cast;
    private String director;
}
