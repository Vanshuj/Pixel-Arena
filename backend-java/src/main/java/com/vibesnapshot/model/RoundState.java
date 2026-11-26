package com.vibesnapshot.model;

public class RoundState {
    private boolean active;
    private String startTime;
    private String analysisEndTime;
    private String endTime;
    private int buildDuration;
    private int analysisDuration;
    private String phase;

    public RoundState() {
    }

    public static RoundState defaultRound() {
        RoundState round = new RoundState();
        round.setActive(false);
        round.setStartTime(null);
        round.setAnalysisEndTime(null);
        round.setEndTime(null);
        round.setBuildDuration(30);
        round.setAnalysisDuration(2);
        round.setPhase("idle");
        return round;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getAnalysisEndTime() {
        return analysisEndTime;
    }

    public void setAnalysisEndTime(String analysisEndTime) {
        this.analysisEndTime = analysisEndTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getBuildDuration() {
        return buildDuration;
    }

    public void setBuildDuration(int buildDuration) {
        this.buildDuration = buildDuration;
    }

    public int getAnalysisDuration() {
        return analysisDuration;
    }

    public void setAnalysisDuration(int analysisDuration) {
        this.analysisDuration = analysisDuration;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }
}
