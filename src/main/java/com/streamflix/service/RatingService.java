package com.streamflix.service;

import com.streamflix.dto.request.RatingRequest;
import com.streamflix.entity.Movie;
import com.streamflix.entity.Rating;
import com.streamflix.entity.User;
import com.streamflix.exception.ResourceNotFoundException;
import com.streamflix.repository.MovieRepository;
import com.streamflix.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final MovieRepository movieRepository;
    private final MovieService movieService;

    @Transactional
    public void rateMovie(User user, RatingRequest request) {
        Long movieId = request.getMovieId();
        if (movieId == null) {
            throw new ResourceNotFoundException("Movie ID cannot be null");
        }
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + movieId));

        Rating rating = ratingRepository.findByUserIdAndMovieId(user.getId(), movie.getId())
                .orElseGet(() -> Rating.builder().user(user).movie(movie).build());

        rating.setScore(request.getScore());
        ratingRepository.save(rating);

        Double newAverage = ratingRepository.findAverageScoreForMovie(movie.getId());
        movieService.recalculateAverageRating(movie.getId(), newAverage);
    }

    @Transactional
    public void removeRating(User user, Long movieId) {
        Rating rating = ratingRepository.findByUserIdAndMovieId(user.getId(), movieId)
                .orElseThrow(() -> new ResourceNotFoundException("You have not rated this movie"));
        ratingRepository.delete(rating);

        Double newAverage = ratingRepository.findAverageScoreForMovie(movieId);
        movieService.recalculateAverageRating(movieId, newAverage);
    }
}
