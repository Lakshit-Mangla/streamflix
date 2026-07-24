package com.streamflix.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ratings",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_movie_rating", columnNames = {"user_id", "movie_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    /** 1 to 5 stars */
    @Column(nullable = false)
    private Integer score;

    private LocalDateTime ratedAt;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        ratedAt = LocalDateTime.now();
    }
}
