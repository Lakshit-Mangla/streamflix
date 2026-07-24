package com.streamflix.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieRequest {

    @NotBlank
    private String title;

    private String description;

    private Integer releaseYear;

    private Integer durationMinutes;

    private String posterUrl;

    private String videoUrl;

    @NotNull
    private String contentType; // MOVIE, SERIES, DOCUMENTARY

    private String cast;

    private String director;

    private Set<String> genres;
}
