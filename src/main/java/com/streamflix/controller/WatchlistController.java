package com.streamflix.controller;

import com.streamflix.dto.response.PageResponse;
import com.streamflix.dto.response.WatchlistResponse;
import com.streamflix.entity.User;
import com.streamflix.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;

    @PostMapping("/{movieId}")
    public ResponseEntity<WatchlistResponse> addToWatchlist(
            @AuthenticationPrincipal User user,
            @PathVariable Long movieId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(watchlistService.addToWatchlist(user, movieId));
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> removeFromWatchlist(
            @AuthenticationPrincipal User user,
            @PathVariable Long movieId) {
        watchlistService.removeFromWatchlist(user, movieId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PageResponse<WatchlistResponse>> getWatchlist(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PageResponse.from(watchlistService.getWatchlist(user, page, size)));
    }
}
