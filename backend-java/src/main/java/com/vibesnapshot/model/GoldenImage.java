package com.vibesnapshot.model;

public class GoldenImage {
    private String filename;
    private String originalName;
    private String path;
    private String url;
    private String uploadedAt;

    public GoldenImage() {
    }

    public GoldenImage(String filename, String originalName, String path, String url, String uploadedAt) {
        this.filename = filename;
        this.originalName = originalName;
        this.path = path;
        this.url = url;
        this.uploadedAt = uploadedAt;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(String uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
