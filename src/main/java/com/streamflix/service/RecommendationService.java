package com.streamflix.service;

import com.streamflix.dto.response.MovieResponse;
import com.streamflix.entity.Genre;
import com.streamflix.entity.Movie;
import com.streamflix.entity.User;
import com.streamflix.entity.WatchHistory;
import com.streamflix.repository.MovieRepository;
import com.streamflix.repository.RatingRepository;
import com.streamflix.repository.WatchHistoryRepository;
import com.streamflix.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Hybrid recommendation engine:
 *   1. Content-based: builds a genre-affinity profile from the user's watch
 *      history and highly-rated movies (score >= 4), then ranks unseen movies
 *      that share those genres, weighted by the movie's own average rating.
 *   2. Cold-start fallback: brand-new users with no signal get trending +
 *      top-rated titles instead of an empty list.
 *   3. "Because you watched X": similar-title suggestions keyed off the
 *      single most recent watch.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private static final int CANDIDATE_POOL_SIZE = 50;

    private final WatchHistoryRepository watchHistoryRepository;
    private final RatingRepository ratingRepository;
    private final MovieRepository movieRepository;

    public List<MovieResponse> getPersonalizedRecommendations(User user, int limit) {
        List<WatchHistory> history = watchHistoryRepository.findTop50ByUserOrderByLastWatchedAtDesc(user);
        List<Long> highlyRatedIds = ratingRepository.findHighlyRatedMovieIdsByUser(user.getId());

        Set<Long> excludedMovieIds = new HashSet<>();
        history.forEach(h -> excludedMovieIds.add(h.getMovie().getId()));

        Map<String, Integer> genreAffinity = buildGenreAffinity(history, highlyRatedIds);

        if (genreAffinity.isEmpty()) {
            // Cold start: no watch/rating signal yet -> popularity-based fallback
            return fallbackRecommendations(limit);
        }

        List<String> rankedGenres = genreAffinity.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();

        List<Long> excludeList = excludedMovieIds.isEmpty() ? List.of(-1L) : new ArrayList<>(excludedMovieIds);

        List<Movie> candidates = movieRepository.findSimilarByGenres(
                rankedGenres, excludeList, PageRequest.of(0, CANDIDATE_POOL_SIZE));

        List<MovieResponse> recommendations = candidates.stream()
                .sorted((a, b) -> Double.compare(
                        score(b, genreAffinity), score(a, genreAffinity)))
                .limit(limit)
                .map(EntityMapper::toMovieResponse)
                .collect(Collectors.toList());

        // Top up with trending titles if genre-based results are thin
        if (recommendations.size() < limit) {
            List<Long> alreadyIncluded = recommendations.stream().map(MovieResponse::getId).toList();
            movieRepository.findTrending(PageRequest.of(0, limit * 2)).stream()
                    .filter(m -> !excludedMovieIds.contains(m.getId()) && !alreadyIncluded.contains(m.getId()))
                    .limit((long) limit - recommendations.size())
                    .forEach(m -> recommendations.add(EntityMapper.toMovieResponse(m)));
        }

        return recommendations;
    }

    /** "Because you watched {title}" - similar titles keyed off one recent watch. */
    public List<MovieResponse> getSimilarToMovie(Long movieId, int limit) {
        Movie source = movieRepository.findById(movieId).orElse(null);
        if (source == null || source.getGenres().isEmpty()) {
            return fallbackRecommendations(limit);
        }

        List<String> genreNames = source.getGenres().stream().map(Genre::getName).toList();

        return movieRepository.findSimilarByGenres(genreNames, List.of(movieId), PageRequest.of(0, limit))
                .stream()
                .map(EntityMapper::toMovieResponse)
                .collect(Collectors.toList());
    }

    private List<MovieResponse> fallbackRecommendations(int limit) {
        int half = Math.max(1, limit / 2);
        LinkedHashSet<Movie> pool = new LinkedHashSet<>();
        pool.addAll(movieRepository.findTrending(PageRequest.of(0, half)));
        pool.addAll(movieRepository.findTopRated(PageRequest.of(0, limit - pool.size())));
        return pool.stream()
                .limit(limit)
                .map(EntityMapper::toMovieResponse)
                .collect(Collectors.toList());
    }

    /**
     * Weighted genre affinity: each completed watch contributes 2 points per
     * genre, each in-progress watch 1 point, and each 4-5 star rating adds
     * 3 points -- so explicit "I loved this" signal outweighs passive viewing.
     */
    private Map<String, Integer> buildGenreAffinity(List<WatchHistory> history, List<Long> highlyRatedIds) {
        Map<String, Integer> affinity = new HashMap<>();

        for (WatchHistory h : history) {
            int weight = h.isCompleted() ? 2 : 1;
            for (Genre g : h.getMovie().getGenres()) {
                affinity.merge(g.getName(), weight, Integer::sum);
            }
        }

        if (!highlyRatedIds.isEmpty()) {
            movieRepository.findAllById(highlyRatedIds).forEach(m ->
                    m.getGenres().forEach(g -> affinity.merge(g.getName(), 3, Integer::sum)));
        }

        return affinity;
    }

    private double score(Movie movie, Map<String, Integer> genreAffinity) {
        double genreScore = movie.getGenres().stream()
                .mapToInt(g -> genreAffinity.getOrDefault(g.getName(), 0))
                .sum();
        double ratingBoost = movie.getAverageRating() != null ? movie.getAverageRating() : 0.0;
        return genreScore + ratingBoost; // genre match dominates, rating breaks ties
    }
}
