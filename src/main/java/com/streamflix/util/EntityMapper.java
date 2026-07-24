package com.streamflix.util;

import com.streamflix.dto.response.MovieResponse;
import com.streamflix.dto.response.WatchHistoryResponse;
import com.streamflix.dto.response.WatchlistResponse;
import com.streamflix.entity.Movie;
import com.streamflix.entity.WatchHistory;
import com.streamflix.entity.WatchlistItem;

import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

public final class EntityMapper {

    private EntityMapper() {
    }

    public static MovieResponse toMovieResponse(Movie movie) {
        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .releaseYear(movie.getReleaseYear())
                .durationMinutes(movie.getDurationMinutes())
                .posterUrl(movie.getPosterUrl())
                .videoUrl(movie.getVideoUrl())
                .contentType(movie.getContentType() != null ? movie.getContentType().name() : null)
                .averageRating(movie.getAverageRating())
                .viewCount(movie.getViewCount())
                .genres(movie.getGenres() == null ? Collections.emptySet() : movie.getGenres().stream()
                    .filter(Objects::nonNull)
                    .map(g -> g.getName())
                    .collect(Collectors.toSet()))
                .cast(movie.getCast())
                .director(movie.getDirector())
                .build();
    }

    public static WatchHistoryResponse toWatchHistoryResponse(WatchHistory history) {
        return WatchHistoryResponse.builder()
                .id(history.getId())
                .movie(toMovieResponse(history.getMovie()))
                .progressMinutes(history.getProgressMinutes())
                .completed(history.isCompleted())
                .lastWatchedAt(history.getLastWatchedAt())
                .build();
    }

    public static WatchlistResponse toWatchlistResponse(WatchlistItem item) {
        return WatchlistResponse.builder()
                .id(item.getId())
                .movie(toMovieResponse(item.getMovie()))
                .addedAt(item.getAddedAt())
                .build();
    }
}
