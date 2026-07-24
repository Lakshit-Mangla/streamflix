package com.streamflix.service;

import com.streamflix.dto.request.MovieRequest;
import com.streamflix.dto.response.MovieResponse;
import com.streamflix.entity.Genre;
import com.streamflix.entity.Movie;
import com.streamflix.exception.ResourceNotFoundException;
import com.streamflix.repository.GenreRepository;
import com.streamflix.repository.MovieRepository;
import com.streamflix.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;

    @Transactional
    public MovieResponse createMovie(MovieRequest request) {
        Movie movie = Movie.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .releaseYear(request.getReleaseYear())
                .durationMinutes(request.getDurationMinutes())
                .posterUrl(request.getPosterUrl())
                .videoUrl(request.getVideoUrl())
                .contentType(Movie.ContentType.valueOf(request.getContentType().toUpperCase()))
                .cast(request.getCast())
                .director(request.getDirector())
                .genres(resolveGenres(request.getGenres()))
                .viewCount(0L)
                .build();

        return EntityMapper.toMovieResponse(movieRepository.save(movie));
    }

    @Transactional
    public MovieResponse updateMovie(Long id, MovieRequest request) {
        Movie movie = getMovieEntity(id);

        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setReleaseYear(request.getReleaseYear());
        movie.setDurationMinutes(request.getDurationMinutes());
        movie.setPosterUrl(request.getPosterUrl());
        movie.setVideoUrl(request.getVideoUrl());
        if (request.getContentType() != null) {
            movie.setContentType(Movie.ContentType.valueOf(request.getContentType().toUpperCase()));
        }
        movie.setCast(request.getCast());
        movie.setDirector(request.getDirector());
        if (request.getGenres() != null) {
            movie.setGenres(resolveGenres(request.getGenres()));
        }

        return EntityMapper.toMovieResponse(movieRepository.save(movie));
    }

    @Transactional
    public void deleteMovie(Long id) {
        Movie movie = getMovieEntity(id);
        movieRepository.delete(movie);
    }

    public MovieResponse getMovieById(Long id) {
        return EntityMapper.toMovieResponse(getMovieEntity(id));
    }

    protected Movie getMovieEntity(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
    }

    public Page<MovieResponse> getAllMovies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return movieRepository.findAll(pageable).map(EntityMapper::toMovieResponse);
    }

    /**
     * Multi-criteria search: free-text title match plus optional filters on
     * genre, release-year range, and content type. Built with Specifications
     * so filters combine cleanly regardless of which ones are supplied.
     */
    public Page<MovieResponse> searchMovies(String query, String genre, Integer yearFrom, Integer yearTo,
                                             String contentType, int page, int size) {

        Specification<Movie> spec = Specification.where(null);

        if (query != null && !query.isBlank()) {
            String like = "%" + query.toLowerCase() + "%";
            spec = spec.and((root, cq, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), like),
                    cb.like(cb.lower(root.get("description")), like),
                    cb.like(cb.lower(root.get("cast")), like),
                    cb.like(cb.lower(root.get("director")), like)
            ));
        }

        if (genre != null && !genre.isBlank()) {
            spec = spec.and((root, cq, cb) -> {
                cq.distinct(true);
                var genreJoin = root.join("genres");
                return cb.equal(cb.lower(genreJoin.get("name")), genre.toLowerCase());
            });
        }

        if (yearFrom != null) {
            spec = spec.and((root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("releaseYear"), yearFrom));
        }

        if (yearTo != null) {
            spec = spec.and((root, cq, cb) -> cb.lessThanOrEqualTo(root.get("releaseYear"), yearTo));
        }

        if (contentType != null && !contentType.isBlank()) {
            spec = spec.and((root, cq, cb) ->
                    cb.equal(root.get("contentType"), Movie.ContentType.valueOf(contentType.toUpperCase())));
        }

        Pageable pageable = PageRequest.of(page, size);
        return movieRepository.findAll(spec, pageable).map(EntityMapper::toMovieResponse);
    }

    public List<MovieResponse> getTrending(int limit) {
        return movieRepository.findTrending(PageRequest.of(0, limit)).stream()
                .map(EntityMapper::toMovieResponse)
                .collect(Collectors.toList());
    }

    public List<MovieResponse> getTopRated(int limit) {
        return movieRepository.findTopRated(PageRequest.of(0, limit)).stream()
                .map(EntityMapper::toMovieResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void incrementViewCount(Long movieId) {
        Movie movie = getMovieEntity(movieId);
        movie.setViewCount(movie.getViewCount() + 1);
        movieRepository.save(movie);
    }

    @Transactional
    public void recalculateAverageRating(Long movieId, Double newAverage) {
        Movie movie = getMovieEntity(movieId);
        movie.setAverageRating(newAverage == null ? 0.0 : newAverage);
        movieRepository.save(movie);
    }

    private Set<Genre> resolveGenres(Set<String> genreNames) {
        if (genreNames == null) return Set.of();
        return genreNames.stream()
                .map(name -> genreRepository.findByNameIgnoreCase(name)
                        .orElseGet(() -> genreRepository.save(Genre.builder().name(name).build())))
                .collect(Collectors.toSet());
    }
}
