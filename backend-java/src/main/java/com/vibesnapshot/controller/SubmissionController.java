package com.vibesnapshot.controller;

import com.vibesnapshot.dto.LeaderboardEntry;
import com.vibesnapshot.model.ArenaState;
import com.vibesnapshot.model.RoundState;
import com.vibesnapshot.model.Submission;
import com.vibesnapshot.repository.SubmissionRepository;
import com.vibesnapshot.service.ArenaService;
import com.vibesnapshot.service.ImageCompareService;
import com.vibesnapshot.service.StorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final ArenaService arenaService;
    private final StorageService storageService;
    private final ImageCompareService imageCompareService;
    private final SubmissionRepository submissionRepository;

    public SubmissionController(
            ArenaService arenaService,
            StorageService storageService,
            ImageCompareService imageCompareService,
            SubmissionRepository submissionRepository
    ) {
        this.arenaService = arenaService;
        this.storageService = storageService;
        this.imageCompareService = imageCompareService;
        this.submissionRepository = submissionRepository;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> submitScreenshot(
            @RequestParam(value = "teamName", required = false) String teamName,
            @RequestParam(value = "githubUrl", required = false) String githubUrl,
            @RequestParam(value = "screenshot", required = false) MultipartFile screenshot
    ) {
        if (teamName == null || teamName.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Team name is required"));
        }

        if (screenshot == null || screenshot.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Screenshot is required"));
        }

        String contentType = screenshot.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Only image files allowed"));
        }

        ArenaState state = arenaService.getState();
        if (state.getGoldenImage() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "No golden image set by admin yet"));
        }

        RoundState currentRound = arenaService.syncRound();
        if ("locked".equals(currentRound.getPhase())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Submissions are closed for this round"));
        }

        String trimmedTeam = teamName.trim();
        StorageService.StoredFile stored = null;
        Path diffPath = null;

        try {
            stored = storageService.saveSubmissionImage(screenshot);
            String id = UUID.randomUUID().toString();
            String diffFilename = "diff-" + id + ".png";
            diffPath = storageService.getDiffsDir().resolve(diffFilename);

            ImageCompareService.ComparisonResult result = imageCompareService.compareImages(
                    Paths.get(state.getGoldenImage().getPath()),
                    Paths.get(stored.absolutePath()),
                    diffPath
            );

            Submission submission = new Submission();
            submission.setId(id);
            submission.setTeamName(trimmedTeam);
            submission.setGithubUrl((githubUrl != null && !githubUrl.isBlank()) ? githubUrl.trim() : null);
            submission.setScreenshotUrl(stored.url());
            submission.setScreenshotPath(stored.absolutePath());
            submission.setDiffUrl("/uploads/diffs/" + diffFilename);
            submission.setSimilarity(result.similarity());
            submission.setDiffPixels(result.diffPixels());
            submission.setTotalPixels(result.totalPixels());
            submission.setMatchedPixels(result.matchedPixels());
            submission.setSubmittedAt(Instant.now().toString());

            Optional<Submission> existing = submissionRepository.findByTeamNameLower(trimmedTeam.toLowerCase());
            if (existing.isPresent()) {
                Submission old = existing.get();
                storageService.deleteFileIfExists(old.getScreenshotPath());
                storageService.deleteFileIfExists(
                        storageService.getDiffsDir().resolve("diff-" + old.getId() + ".png").toString()
                );
                submissionRepository.delete(old);
            }

            submissionRepository.save(submission);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("submission", submission);
            return ResponseEntity.ok(response);
        } catch (Exception err) {
            if (stored != null) {
                storageService.deleteFileIfExists(stored.absolutePath());
            }
            if (diffPath != null) {
                storageService.deleteFileIfExists(diffPath.toString());
            }
            String message = (err.getMessage() != null && !err.getMessage().isBlank())
                    ? err.getMessage()
                    : "Failed to process submission";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", message));
        }
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<Map<String, Object>> getLeaderboard() {
        List<Submission> sorted = submissionRepository.findAllByOrderBySimilarityDesc();
        List<LeaderboardEntry> leaderboard = new ArrayList<>(sorted.size());
        for (int i = 0; i < sorted.size(); i++) {
            leaderboard.add(LeaderboardEntry.from(sorted.get(i), i + 1));
        }
        RoundState round = arenaService.getState().getRound();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("leaderboard", leaderboard);
        response.put("round", round);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/team/{teamName}")
    public ResponseEntity<Map<String, Object>> getTeamSubmission(@PathVariable String teamName) {
        Optional<Submission> submission = submissionRepository.findByTeamNameLower(teamName.trim().toLowerCase());
        if (submission.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Team not found"));
        }
        return ResponseEntity.ok(Map.of("submission", submission.get()));
    }
}
