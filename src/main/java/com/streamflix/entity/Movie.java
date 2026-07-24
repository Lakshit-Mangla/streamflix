package com.streamflix.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "movies", indexes = {
        @Index(name = "idx_movie_title", columnList = "title"),
        @Index(name = "idx_movie_release_year", columnList = "releaseYear")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    private Integer releaseYear;

    private Integer durationMinutes;

    @Column(length = 500)
    private String posterUrl;

    @Column(length = 500)
    private String videoUrl;

    @Enumerated(EnumType.STRING)
    private ContentType contentType; // MOVIE, SERIES, DOCUMENTARY

    private Double averageRating;

    @Builder.Default
    private Long viewCount = 0L;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "movie_genres",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @Builder.Default
    private Set<Genre> genres = new HashSet<>();

    @Column(name = "cast_members", length = 300)
    private String cast;

    @Column(length = 150)
    private String director;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (averageRating == null) averageRating = 0.0;
    }

    public enum ContentType {
        MOVIE, SERIES, DOCUMENTARY
    }
}
