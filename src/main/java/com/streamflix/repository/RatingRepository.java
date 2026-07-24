package com.streamflix.repository;

import com.streamflix.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByUserIdAndMovieId(Long userId, Long movieId);

    List<Rating> findByUserId(Long userId);

    @Query("select avg(r.score) from Rating r where r.movie.id = :movieId")
    Double findAverageScoreForMovie(@Param("movieId") Long movieId);

    @Query("select r.movie.id from Rating r where r.user.id = :userId and r.score >= 4")
    List<Long> findHighlyRatedMovieIdsByUser(@Param("userId") Long userId);
}
