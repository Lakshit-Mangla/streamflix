package com.streamflix.controller;

import com.streamflix.dto.response.MovieResponse;
import com.streamflix.entity.User;
import com.streamflix.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/for-you")
    public ResponseEntity<List<MovieResponse>> getPersonalized(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(recommendationService.getPersonalizedRecommendations(user, limit));
    }

    @GetMapping("/similar/{movieId}")
    public ResponseEntity<List<MovieResponse>> getSimilar(
            @PathVariable Long movieId,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(recommendationService.getSimilarToMovie(movieId, limit));
    }
}
