package com.vibesnapshot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "submissions")
public class Submission {
    @Id
    private String id;
    private String teamName;

    @JsonIgnore
    @Indexed(unique = true)
    private String teamNameLower;

    private String githubUrl;
    private String screenshotUrl;
    private String screenshotPath;
    private String diffUrl;
    private double similarity;
    private long diffPixels;
    private long totalPixels;
    private long matchedPixels;
    private String submittedAt;

    public Submission() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
        this.teamNameLower = teamName != null ? teamName.trim().toLowerCase() : null;
    }

    public String getTeamNameLower() {
        return teamNameLower;
    }

    public void setTeamNameLower(String teamNameLower) {
        this.teamNameLower = teamNameLower;
    }

    public String getGithubUrl() {
        return githubUrl;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }

    public String getScreenshotUrl() {
        return screenshotUrl;
    }

    public void setScreenshotUrl(String screenshotUrl) {
        this.screenshotUrl = screenshotUrl;
    }

    public String getScreenshotPath() {
        return screenshotPath;
    }

    public void setScreenshotPath(String screenshotPath) {
        this.screenshotPath = screenshotPath;
    }

    public String getDiffUrl() {
        return diffUrl;
    }

    public void setDiffUrl(String diffUrl) {
        this.diffUrl = diffUrl;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    public long getDiffPixels() {
        return diffPixels;
    }

    public void setDiffPixels(long diffPixels) {
        this.diffPixels = diffPixels;
    }

    public long getTotalPixels() {
        return totalPixels;
    }

    public void setTotalPixels(long totalPixels) {
        this.totalPixels = totalPixels;
    }

    public long getMatchedPixels() {
        return matchedPixels;
    }

    public void setMatchedPixels(long matchedPixels) {
        this.matchedPixels = matchedPixels;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(String submittedAt) {
        this.submittedAt = submittedAt;
    }
}
