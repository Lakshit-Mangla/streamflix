package com.streamflix.repository;

import com.streamflix.entity.User;
import com.streamflix.entity.WatchlistItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WatchlistRepository extends JpaRepository<WatchlistItem, Long> {

    Page<WatchlistItem> findByUserOrderByAddedAtDesc(User user, Pageable pageable);

    Optional<WatchlistItem> findByUserIdAndMovieId(Long userId, Long movieId);

    boolean existsByUserIdAndMovieId(Long userId, Long movieId);

    void deleteByUserIdAndMovieId(Long userId, Long movieId);
}
