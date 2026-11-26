package com.vibesnapshot.controller;

import com.vibesnapshot.model.ArenaState;
import com.vibesnapshot.model.GoldenImage;
import com.vibesnapshot.model.Submission;
import com.vibesnapshot.repository.SubmissionRepository;
import com.vibesnapshot.service.ArenaService;
import com.vibesnapshot.service.StorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ArenaService arenaService;
    private final StorageService storageService;
    private final SubmissionRepository submissionRepository;

    public AdminController(
            ArenaService arenaService,
            StorageService storageService,
            SubmissionRepository submissionRepository
    ) {
        this.arenaService = arenaService;
        this.storageService = storageService;
        this.submissionRepository = submissionRepository;
    }

    @PostMapping("/golden-image")
    public ResponseEntity<Map<String, Object>> uploadGoldenImage(
            @RequestParam(value = "image", required = false) MultipartFile image
    ) throws IOException {
        if (image == null || image.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "No image uploaded"));
        }

        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Only image files allowed"));
        }

        StorageService.StoredFile stored = storageService.saveGoldenImage(image);
        GoldenImage goldenImage = new GoldenImage(
                stored.filename(),
                image.getOriginalFilename(),
                stored.absolutePath(),
                stored.url(),
                Instant.now().toString()
        );
        arenaService.saveGoldenImage(goldenImage);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("goldenImage", goldenImage);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/golden-image")
    public ResponseEntity<Map<String, Object>> deleteGoldenImage() {
        ArenaState state = arenaService.getState();
        if (state.getGoldenImage() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No golden image to delete"));
        }

        storageService.deleteFileIfExists(state.getGoldenImage().getPath());
        arenaService.clearGoldenImage();
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/golden-image")
    public ResponseEntity<Map<String, Object>> getGoldenImage() {
        ArenaState state = arenaService.getState();
        return ResponseEntity.ok(Collections.singletonMap("goldenImage", state.getGoldenImage()));
    }

    @GetMapping("/submissions")
    public ResponseEntity<Map<String, Object>> getAllSubmissions() {
        List<Submission> sorted = submissionRepository.findAllByOrderBySimilarityDesc();
        return ResponseEntity.ok(Map.of("submissions", sorted));
    }

    @DeleteMapping("/submissions/{id}")
    public ResponseEntity<Map<String, Object>> deleteSubmission(@PathVariable String id) {
        Optional<Submission> existing = submissionRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Not found"));
        }
        Submission submission = existing.get();
        storageService.deleteFileIfExists(submission.getScreenshotPath());
        storageService.deleteFileIfExists(
                storageService.getDiffsDir().resolve("diff-" + submission.getId() + ".png").toString()
        );
        submissionRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetAll() throws IOException {
        submissionRepository.deleteAll();
        arenaService.resetRoundPreservingGolden();
        storageService.clearSubmissionsAndDiffs();
        return ResponseEntity.ok(Map.of("success", true));
    }
}
