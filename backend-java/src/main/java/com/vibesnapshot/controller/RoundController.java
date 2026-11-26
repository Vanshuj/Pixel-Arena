package com.vibesnapshot.controller;

import com.vibesnapshot.model.ArenaState;
import com.vibesnapshot.model.RoundState;
import com.vibesnapshot.service.ArenaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/round")
public class RoundController {

    private final ArenaService arenaService;

    public RoundController(ArenaService arenaService) {
        this.arenaService = arenaService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getRoundStatus() {
        ArenaState state = arenaService.getState();
        return ResponseEntity.ok(Map.of("round", state.getRound()));
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startRound(
            @RequestBody(required = false) Map<String, Object> body
    ) {
        ArenaState state = arenaService.getState();
        if (state.getGoldenImage() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Upload a golden image before starting a round"));
        }

        int buildDuration = extractInt(body, "buildDuration", 30);
        int analysisDuration = extractInt(body, "analysisDuration", 2);

        RoundState round = arenaService.startRound(buildDuration, analysisDuration);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("round", round);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/lock")
    public ResponseEntity<Map<String, Object>> lockRound() {
        RoundState round = arenaService.lockRound();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("round", round);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncRound() {
        RoundState round = arenaService.syncRound();
        return ResponseEntity.ok(Map.of("round", round));
    }

    private int extractInt(Map<String, Object> body, String key, int defaultValue) {
        if (body == null || !body.containsKey(key) || body.get(key) == null) {
            return defaultValue;
        }
        Object val = body.get(key);
        if (val instanceof Number num) {
            return num.intValue();
        }
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
