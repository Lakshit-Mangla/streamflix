package com.streamflix.repository;

import com.streamflix.entity.User;
import com.streamflix.entity.WatchHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WatchHistoryRepository extends JpaRepository<WatchHistory, Long> {

    Page<WatchHistory> findByUserOrderByLastWatchedAtDesc(User user, Pageable pageable);

    Optional<WatchHistory> findByUserIdAndMovieId(Long userId, Long movieId);

    List<WatchHistory> findTop50ByUserOrderByLastWatchedAtDesc(User user);

    boolean existsByUserIdAndMovieIdAndCompletedTrue(Long userId, Long movieId);
}
