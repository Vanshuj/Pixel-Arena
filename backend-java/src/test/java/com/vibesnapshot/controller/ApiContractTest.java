package com.vibesnapshot.controller;

import com.vibesnapshot.model.ArenaState;
import com.vibesnapshot.model.GoldenImage;
import com.vibesnapshot.model.RoundState;
import com.vibesnapshot.model.Submission;
import com.vibesnapshot.repository.SubmissionRepository;
import com.vibesnapshot.service.ArenaService;
import com.vibesnapshot.service.ImageCompareService;
import com.vibesnapshot.service.StorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({
        HealthController.class,
        AdminController.class,
        RoundController.class,
        SubmissionController.class
})
class ApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArenaService arenaService;

    @MockBean
    private StorageService storageService;

    @MockBean
    private ImageCompareService imageCompareService;

    @MockBean
    private SubmissionRepository submissionRepository;

    @Test
    void healthReturnsOk() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void getGoldenImageWhenUnsetReturnsNullField() throws Exception {
        ArenaState state = new ArenaState();
        when(arenaService.getState()).thenReturn(state);

        mockMvc.perform(get("/api/admin/golden-image"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goldenImage").doesNotExist());
    }

    @Test
    void startRoundRequiresGoldenImage() throws Exception {
        ArenaState state = new ArenaState();
        when(arenaService.getState()).thenReturn(state);

        mockMvc.perform(post("/api/round/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"buildDuration\":30,\"analysisDuration\":2}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Upload a golden image before starting a round"));
    }

    @Test
    void startRoundSucceedsWhenGoldenSet() throws Exception {
        ArenaState state = new ArenaState();
        state.setGoldenImage(new GoldenImage("golden.png", "g.png", "/tmp/golden.png", "/uploads/golden/golden.png", "2026-01-01T00:00:00Z"));
        when(arenaService.getState()).thenReturn(state);

        RoundState activeRound = RoundState.defaultRound();
        activeRound.setActive(true);
        activeRound.setPhase("analysis");
        when(arenaService.startRound(anyInt(), anyInt())).thenReturn(activeRound);

        mockMvc.perform(post("/api/round/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"buildDuration\":25,\"analysisDuration\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.round.phase").value("analysis"))
                .andExpect(jsonPath("$.round.active").value(true));
    }

    @Test
    void submitScreenshotRejectsWhenLocked() throws Exception {
        ArenaState state = new ArenaState();
        state.setGoldenImage(new GoldenImage("golden.png", "g.png", "/tmp/golden.png", "/uploads/golden/golden.png", "2026-01-01T00:00:00Z"));
        when(arenaService.getState()).thenReturn(state);

        RoundState locked = RoundState.defaultRound();
        locked.setPhase("locked");
        when(arenaService.syncRound()).thenReturn(locked);

        MockMultipartFile file = new MockMultipartFile("screenshot", "test.png", "image/png", new byte[]{1, 2, 3});
        mockMvc.perform(multipart("/api/submissions")
                        .file(file)
                        .param("teamName", "Pixel Ninjas"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Submissions are closed for this round"));
    }

    @Test
    void submitScreenshotReturnsScoreAndDiffUrls() throws Exception {
        ArenaState state = new ArenaState();
        state.setGoldenImage(new GoldenImage("golden.png", "g.png", "/tmp/golden.png", "/uploads/golden/golden.png", "2026-01-01T00:00:00Z"));
        when(arenaService.getState()).thenReturn(state);

        RoundState build = RoundState.defaultRound();
        build.setActive(true);
        build.setPhase("build");
        when(arenaService.syncRound()).thenReturn(build);
        when(storageService.getDiffsDir()).thenReturn(Path.of("uploads/diffs"));
        when(storageService.saveSubmissionImage(any())).thenReturn(
                new StorageService.StoredFile("sub.png", "/tmp/sub.png", "/uploads/submissions/sub.png")
        );
        when(imageCompareService.compareImages(any(), any(), any())).thenReturn(
                new ImageCompareService.ComparisonResult(92.5, 750, 10000, 9250, 100, 100)
        );
        when(submissionRepository.findByTeamNameLower("pixel ninjas")).thenReturn(Optional.empty());
        when(submissionRepository.save(any(Submission.class))).thenAnswer(inv -> inv.getArgument(0));

        MockMultipartFile file = new MockMultipartFile("screenshot", "test.png", "image/png", new byte[]{1, 2, 3});
        mockMvc.perform(multipart("/api/submissions")
                        .file(file)
                        .param("teamName", "Pixel Ninjas")
                        .param("githubUrl", "https://github.com/example/repo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.submission.teamName").value("Pixel Ninjas"))
                .andExpect(jsonPath("$.submission.similarity").value(92.5))
                .andExpect(jsonPath("$.submission.matchedPixels").value(9250))
                .andExpect(jsonPath("$.submission.screenshotUrl").value("/uploads/submissions/sub.png"));
    }

    @Test
    void leaderboardReturnsRankedSubmissions() throws Exception {
        Submission s1 = new Submission();
        s1.setId("1");
        s1.setTeamName("Alpha");
        s1.setSimilarity(95.0);

        ArenaState state = new ArenaState();
        when(arenaService.getState()).thenReturn(state);
        when(submissionRepository.findAllByOrderBySimilarityDesc()).thenReturn(List.of(s1));

        mockMvc.perform(get("/api/submissions/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leaderboard[0].rank").value(1))
                .andExpect(jsonPath("$.leaderboard[0].teamName").value("Alpha"))
                .andExpect(jsonPath("$.round.phase").value("idle"));
    }
}
