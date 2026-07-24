package com.streamflix.service;

import com.streamflix.dto.request.WatchProgressRequest;
import com.streamflix.dto.response.WatchHistoryResponse;
import com.streamflix.entity.Movie;
import com.streamflix.entity.User;
import com.streamflix.entity.WatchHistory;
import com.streamflix.exception.ResourceNotFoundException;
import com.streamflix.repository.MovieRepository;
import com.streamflix.repository.WatchHistoryRepository;
import com.streamflix.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WatchHistoryService {

    private final WatchHistoryRepository watchHistoryRepository;
    private final MovieRepository movieRepository;
    private final MovieService movieService;

    @Transactional
    public WatchHistoryResponse recordProgress(User user, WatchProgressRequest request) {
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + request.getMovieId()));

        WatchHistory history = watchHistoryRepository.findByUserIdAndMovieId(user.getId(), movie.getId())
                .orElseGet(() -> {
                    // First time watching this title -> counts as a view
                    movieService.incrementViewCount(movie.getId());
                    return WatchHistory.builder()
                            .user(user)
                            .movie(movie)
                            .progressMinutes(0)
                            .completed(false)
                            .build();
                });

        history.setProgressMinutes(request.getProgressMinutes());
        if (request.getCompleted() != null) {
            history.setCompleted(request.getCompleted());
        } else if (movie.getDurationMinutes() != null
                && request.getProgressMinutes() >= movie.getDurationMinutes()) {
            history.setCompleted(true);
        }

        return EntityMapper.toWatchHistoryResponse(watchHistoryRepository.save(history));
    }

    public Page<WatchHistoryResponse> getHistory(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return watchHistoryRepository.findByUserOrderByLastWatchedAtDesc(user, pageable)
                .map(EntityMapper::toWatchHistoryResponse);
    }

    @Transactional
    public void deleteHistoryEntry(User user, Long movieId) {
        WatchHistory history = watchHistoryRepository.findByUserIdAndMovieId(user.getId(), movieId)
                .orElseThrow(() -> new ResourceNotFoundException("No watch history for this movie"));
        watchHistoryRepository.delete(history);
    }
}
