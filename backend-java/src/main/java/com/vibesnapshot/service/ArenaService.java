package com.vibesnapshot.service;

import com.vibesnapshot.model.ArenaState;
import com.vibesnapshot.model.GoldenImage;
import com.vibesnapshot.model.RoundState;
import com.vibesnapshot.repository.ArenaStateRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ArenaService {

    private final ArenaStateRepository arenaStateRepository;

    public ArenaService(ArenaStateRepository arenaStateRepository) {
        this.arenaStateRepository = arenaStateRepository;
    }

    public synchronized ArenaState getState() {
        return arenaStateRepository.findById(ArenaState.DEFAULT_ID)
                .orElseGet(() -> {
                    ArenaState initial = new ArenaState();
                    initial.setId(ArenaState.DEFAULT_ID);
                    initial.setRound(RoundState.defaultRound());
                    return arenaStateRepository.save(initial);
                });
    }

    public synchronized ArenaState saveGoldenImage(GoldenImage goldenImage) {
        ArenaState state = getState();
        state.setGoldenImage(goldenImage);
        return arenaStateRepository.save(state);
    }

    public synchronized ArenaState clearGoldenImage() {
        ArenaState state = getState();
        state.setGoldenImage(null);
        return arenaStateRepository.save(state);
    }

    public synchronized RoundState startRound(int buildDuration, int analysisDuration) {
        ArenaState state = getState();
        long now = System.currentTimeMillis();
        long analysisEnd = now + analysisDuration * 60_000L;
        long buildEnd = analysisEnd + buildDuration * 60_000L;

        RoundState round = new RoundState();
        round.setActive(true);
        round.setStartTime(Instant.ofEpochMilli(now).toString());
        round.setAnalysisEndTime(Instant.ofEpochMilli(analysisEnd).toString());
        round.setEndTime(Instant.ofEpochMilli(buildEnd).toString());
        round.setBuildDuration(buildDuration);
        round.setAnalysisDuration(analysisDuration);
        round.setPhase("analysis");

        state.setRound(round);
        arenaStateRepository.save(state);
        return round;
    }

    public synchronized RoundState lockRound() {
        ArenaState state = getState();
        RoundState round = state.getRound() != null ? state.getRound() : RoundState.defaultRound();
        round.setPhase("locked");
        round.setActive(false);
        state.setRound(round);
        arenaStateRepository.save(state);
        return round;
    }

    public synchronized RoundState syncRound() {
        ArenaState state = getState();
        RoundState round = state.getRound() != null ? state.getRound() : RoundState.defaultRound();
        if (!round.isActive()) {
            return round;
        }

        long now = System.currentTimeMillis();
        long analysisEnd = Instant.parse(round.getAnalysisEndTime()).toEpochMilli();
        long buildEnd = Instant.parse(round.getEndTime()).toEpochMilli();

        if (now >= buildEnd) {
            round.setPhase("locked");
            round.setActive(false);
        } else if (now >= analysisEnd) {
            round.setPhase("build");
        } else {
            round.setPhase("analysis");
        }

        state.setRound(round);
        arenaStateRepository.save(state);
        return round;
    }

    public synchronized void resetRoundPreservingGolden() {
        ArenaState state = getState();
        RoundState current = state.getRound();
        RoundState reset = RoundState.defaultRound();
        if (current != null) {
            reset.setBuildDuration(current.getBuildDuration() > 0 ? current.getBuildDuration() : 30);
            reset.setAnalysisDuration(current.getAnalysisDuration() > 0 ? current.getAnalysisDuration() : 2);
        }
        state.setRound(reset);
        arenaStateRepository.save(state);
    }
}
