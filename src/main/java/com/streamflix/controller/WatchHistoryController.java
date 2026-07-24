package com.streamflix.controller;

import com.streamflix.dto.request.WatchProgressRequest;
import com.streamflix.dto.response.PageResponse;
import com.streamflix.dto.response.WatchHistoryResponse;
import com.streamflix.entity.User;
import com.streamflix.service.WatchHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/watch-history")
@RequiredArgsConstructor
public class WatchHistoryController {

    private final WatchHistoryService watchHistoryService;

    @PostMapping("/progress")
    public ResponseEntity<WatchHistoryResponse> recordProgress(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody WatchProgressRequest request) {
        return ResponseEntity.ok(watchHistoryService.recordProgress(user, request));
    }

    @GetMapping
    public ResponseEntity<PageResponse<WatchHistoryResponse>> getHistory(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PageResponse.from(watchHistoryService.getHistory(user, page, size)));
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> deleteHistoryEntry(
            @AuthenticationPrincipal User user,
            @PathVariable Long movieId) {
        watchHistoryService.deleteHistoryEntry(user, movieId);
        return ResponseEntity.noContent().build();
    }
}
