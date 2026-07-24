package com.streamflix.controller;

import com.streamflix.dto.request.RatingRequest;
import com.streamflix.entity.User;
import com.streamflix.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    public ResponseEntity<Void> rateMovie(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody RatingRequest request) {
        ratingService.rateMovie(user, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> removeRating(
            @AuthenticationPrincipal User user,
            @PathVariable Long movieId) {
        ratingService.removeRating(user, movieId);
        return ResponseEntity.noContent().build();
    }
}
