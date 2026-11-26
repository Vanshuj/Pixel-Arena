package com.vibesnapshot.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class StorageService {

    private final Path uploadRoot;
    private final Path goldenDir;
    private final Path submissionsDir;
    private final Path diffsDir;

    public StorageService(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.goldenDir = this.uploadRoot.resolve("golden");
        this.submissionsDir = this.uploadRoot.resolve("submissions");
        this.diffsDir = this.uploadRoot.resolve("diffs");
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(uploadRoot);
        Files.createDirectories(goldenDir);
        Files.createDirectories(submissionsDir);
        Files.createDirectories(diffsDir);
    }

    public Path getUploadRoot() {
        return uploadRoot;
    }

    public Path getDiffsDir() {
        return diffsDir;
    }

    public StoredFile saveGoldenImage(MultipartFile file) throws IOException {
        clearDirectory(goldenDir);
        String ext = extractExtension(file.getOriginalFilename());
        String filename = "golden" + ext;
        Path target = goldenDir.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return new StoredFile(filename, target.toString(), "/uploads/golden/" + filename);
    }

    public StoredFile saveSubmissionImage(MultipartFile file) throws IOException {
        String ext = extractExtension(file.getOriginalFilename());
        String shortUuid = UUID.randomUUID().toString().substring(0, 8);
        String filename = "submission-" + System.currentTimeMillis() + "-" + shortUuid + ext;
        Path target = submissionsDir.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return new StoredFile(filename, target.toString(), "/uploads/submissions/" + filename);
    }

    public void deleteFileIfExists(String pathStr) {
        if (pathStr == null || pathStr.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(Paths.get(pathStr));
        } catch (IOException ignored) {
        }
    }

    public void clearSubmissionsAndDiffs() throws IOException {
        clearDirectory(submissionsDir);
        clearDirectory(diffsDir);
    }

    private void clearDirectory(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> files = Files.list(dir)) {
            for (Path p : files.toList()) {
                if (Files.isRegularFile(p)) {
                    Files.deleteIfExists(p);
                }
            }
        }
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null) {
            return ".png";
        }
        int dotIdx = originalFilename.lastIndexOf('.');
        if (dotIdx < 0 || dotIdx == originalFilename.length() - 1) {
            return ".png";
        }
        String ext = originalFilename.substring(dotIdx).toLowerCase();
        return ext.matches("\\.[a-z0-9]{1,8}") ? ext : ".png";
    }

    public record StoredFile(String filename, String absolutePath, String url) {
    }
}
