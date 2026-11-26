package com.vibesnapshot.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "arena_state")
public class ArenaState {
    public static final String DEFAULT_ID = "default";

    @Id
    private String id = DEFAULT_ID;
    private GoldenImage goldenImage;
    private RoundState round = RoundState.defaultRound();

    public ArenaState() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GoldenImage getGoldenImage() {
        return goldenImage;
    }

    public void setGoldenImage(GoldenImage goldenImage) {
        this.goldenImage = goldenImage;
    }

    public RoundState getRound() {
        return round;
    }

    public void setRound(RoundState round) {
        this.round = round;
    }
}
