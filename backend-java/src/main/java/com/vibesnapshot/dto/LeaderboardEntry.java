package com.vibesnapshot.dto;

import com.vibesnapshot.model.Submission;

public class LeaderboardEntry {
    private String id;
    private String teamName;
    private String githubUrl;
    private String screenshotUrl;
    private String screenshotPath;
    private String diffUrl;
    private double similarity;
    private long diffPixels;
    private long totalPixels;
    private long matchedPixels;
    private String submittedAt;
    private int rank;

    public static LeaderboardEntry from(Submission s, int rank) {
        LeaderboardEntry entry = new LeaderboardEntry();
        entry.id = s.getId();
        entry.teamName = s.getTeamName();
        entry.githubUrl = s.getGithubUrl();
        entry.screenshotUrl = s.getScreenshotUrl();
        entry.screenshotPath = s.getScreenshotPath();
        entry.diffUrl = s.getDiffUrl();
        entry.similarity = s.getSimilarity();
        entry.diffPixels = s.getDiffPixels();
        entry.totalPixels = s.getTotalPixels();
        entry.matchedPixels = s.getMatchedPixels();
        entry.submittedAt = s.getSubmittedAt();
        entry.rank = rank;
        return entry;
    }

    public String getId() {
        return id;
    }

    public String getTeamName() {
        return teamName;
    }

    public String getGithubUrl() {
        return githubUrl;
    }

    public String getScreenshotUrl() {
        return screenshotUrl;
    }

    public String getScreenshotPath() {
        return screenshotPath;
    }

    public String getDiffUrl() {
        return diffUrl;
    }

    public double getSimilarity() {
        return similarity;
    }

    public long getDiffPixels() {
        return diffPixels;
    }

    public long getTotalPixels() {
        return totalPixels;
    }

    public long getMatchedPixels() {
        return matchedPixels;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }

    public int getRank() {
        return rank;
    }
}
