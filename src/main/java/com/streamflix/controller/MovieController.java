package com.streamflix.controller;

import com.streamflix.dto.request.MovieRequest;
import com.streamflix.dto.response.MovieResponse;
import com.streamflix.dto.response.PageResponse;
import com.streamflix.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    public ResponseEntity<PageResponse<MovieResponse>> getAllMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PageResponse.from(movieService.getAllMovies(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMovie(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<MovieResponse>> searchMovies(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Integer yearFrom,
            @RequestParam(required = false) Integer yearTo,
            @RequestParam(required = false) String contentType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(PageResponse.from(
                movieService.searchMovies(query, genre, yearFrom, yearTo, contentType, page, size)));
    }

    @GetMapping("/trending")
    public ResponseEntity<List<MovieResponse>> getTrending(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(movieService.getTrending(limit));
    }

    @GetMapping("/top-rated")
    public ResponseEntity<List<MovieResponse>> getTopRated(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(movieService.getTopRated(limit));
    }

    // ----- Admin-only catalog management -----

    @PostMapping("/admin")
    public ResponseEntity<MovieResponse> createMovie(@Valid @RequestBody MovieRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.createMovie(request));
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<MovieResponse> updateMovie(@PathVariable Long id, @Valid @RequestBody MovieRequest request) {
        return ResponseEntity.ok(movieService.updateMovie(id, request));
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }
}
