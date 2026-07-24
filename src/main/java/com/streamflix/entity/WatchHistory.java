package com.streamflix.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "watch_history", indexes = {
        @Index(name = "idx_watch_user", columnList = "user_id"),
        @Index(name = "idx_watch_user_movie", columnList = "user_id, movie_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    /** How many minutes into the movie/episode the user got to. */
    @Builder.Default
    private Integer progressMinutes = 0;

    @Builder.Default
    private boolean completed = false;

    private LocalDateTime lastWatchedAt;

    @Column(updatable = false)
    private LocalDateTime firstWatchedAt;

    @PrePersist
    protected void onCreate() {
        firstWatchedAt = LocalDateTime.now();
        lastWatchedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastWatchedAt = LocalDateTime.now();
    }
}
