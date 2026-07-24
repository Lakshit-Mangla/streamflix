package com.streamflix.service;

import com.streamflix.dto.response.WatchlistResponse;
import com.streamflix.entity.Movie;
import com.streamflix.entity.User;
import com.streamflix.entity.WatchlistItem;
import com.streamflix.exception.DuplicateResourceException;
import com.streamflix.exception.ResourceNotFoundException;
import com.streamflix.repository.MovieRepository;
import com.streamflix.repository.WatchlistRepository;
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
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final MovieRepository movieRepository;

    @Transactional
    public WatchlistResponse addToWatchlist(User user, Long movieId) {
        if (watchlistRepository.existsByUserIdAndMovieId(user.getId(), movieId)) {
            throw new DuplicateResourceException("Movie is already in your watchlist");
        }

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + movieId));

        WatchlistItem item = WatchlistItem.builder()
                .user(user)
                .movie(movie)
                .build();

        return EntityMapper.toWatchlistResponse(watchlistRepository.save(item));
    }

    @Transactional
    public void removeFromWatchlist(User user, Long movieId) {
        if (!watchlistRepository.existsByUserIdAndMovieId(user.getId(), movieId)) {
            throw new ResourceNotFoundException("Movie is not in your watchlist");
        }
        watchlistRepository.deleteByUserIdAndMovieId(user.getId(), movieId);
    }

    public Page<WatchlistResponse> getWatchlist(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return watchlistRepository.findByUserOrderByAddedAtDesc(user, pageable)
                .map(EntityMapper::toWatchlistResponse);
    }
}
