package org.example.librarymanagementsystem.Service;

import lombok.extern.slf4j.Slf4j;
import org.example.librarymanagementsystem.exception.FileUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
@Slf4j
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.allowed-types}")
    private String allowedTypes;

    @Value("${file.max-size}")
    private long maxSize;

    @Value("${file.base-url}")
    private String baseUrl;

    // ─── Initialize upload directory ───────────────
    private Path getUploadPath() {
        try {
            Path path = Paths.get(uploadDir)
                    .toAbsolutePath().normalize();

            // create directory if not exists
            Files.createDirectories(path);
            return path;
        } catch (IOException e) {
            throw new FileUploadException(
                    "Could not create upload directory: " +
                            e.getMessage());
        }
    }

    // ─── Validate file ─────────────────────────────
    private void validateFile(MultipartFile file) {

        // check if file is empty
        if (file == null || file.isEmpty()) {
            throw new FileUploadException(
                    "Please select a file to upload");
        }

        // check file size
        if (file.getSize() > maxSize) {
            throw new FileUploadException(
                    "File size " + formatSize(file.getSize()) +
                            " exceeds maximum allowed size of " +
                            formatSize(maxSize));
        }

        // check file type
        String contentType = file.getContentType();
        List<String> allowed =
                Arrays.asList(allowedTypes.split(","));

        if (contentType == null
                || !allowed.contains(
                contentType.trim())) {
            throw new FileUploadException(
                    "File type '" + contentType +
                            "' is not allowed. " +
                            "Allowed types: " + allowedTypes);
        }

        // check original filename
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new FileUploadException(
                    "Invalid file name");
        }

        // check for path traversal attack
        if (filename.contains("..")) {
            throw new FileUploadException(
                    "Invalid file name — path traversal detected");
        }
    }

    // ─── Generate unique file name ─────────────────
    private String generateFileName(
            MultipartFile file) {

        String originalName = StringUtils.cleanPath(
                Objects.requireNonNull(
                        file.getOriginalFilename()));

        // get file extension
        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension =
                    originalName.substring(dotIndex);
        }

        // UUID + timestamp + extension
        return UUID.randomUUID().toString() +
                "_" +
                System.currentTimeMillis() +
                extension;
    }

    // ─── Store file ────────────────────────────────
    public Map<String, Object> storeFile(
            MultipartFile file) {

        // validate first
        validateFile(file);

        try {
            Path uploadPath = getUploadPath();
            String fileName = generateFileName(file);
            Path targetPath =
                    uploadPath.resolve(fileName);

            // copy file to target location
            Files.copy(
                    file.getInputStream(),
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING);

            // build file URL
            String fileUrl = baseUrl +
                    "/covers/" +
                    fileName;

            log.info("File stored: {} ({})",
                    fileName,
                    formatSize(file.getSize()));

            // return file info
            Map<String, Object> result = new HashMap<>();
            result.put("fileName", fileName);
            result.put("originalName",
                    file.getOriginalFilename());
            result.put("fileUrl", fileUrl);
            result.put("fileType",
                    file.getContentType());
            result.put("fileSize", file.getSize());

            return result;

        } catch (IOException e) {
            log.error("File storage failed: {}",
                    e.getMessage());
            throw new FileUploadException(
                    "Could not store file: " +
                            e.getMessage());
        }
    }

    // ─── Delete file ───────────────────────────────
    public void deleteFile(String fileName) {

        if (fileName == null || fileName.isBlank()) {
            return;
        }

        try {
            Path uploadPath = getUploadPath();
            Path filePath =
                    uploadPath.resolve(fileName).normalize();

            // security check — file must be in upload dir
            if (!filePath.startsWith(uploadPath)) {
                throw new FileUploadException(
                        "Invalid file path");
            }

            Files.deleteIfExists(filePath);

            log.info("File deleted: {}", fileName);

        } catch (IOException e) {
            log.error("File deletion failed: {}",
                    e.getMessage());
        }
    }

    // ─── Load file ─────────────────────────────────
    public Path loadFile(String fileName) {

        Path uploadPath = getUploadPath();
        Path filePath =
                uploadPath.resolve(fileName).normalize();

        // security check
        if (!filePath.startsWith(uploadPath)) {
            throw new org.example
                    .librarymanagementsystem
                    .exception.FileNotFoundException(
                    "Invalid file path");
        }

        if (!Files.exists(filePath)) {
            throw new org.example
                    .librarymanagementsystem
                    .exception.FileNotFoundException(
                    "File not found: " + fileName);
        }

        return filePath;
    }

    // ─── Format file size ──────────────────────────
    private String formatSize(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        else if (bytes < 1024 * 1024)
            return String.format("%.1f KB",
                    bytes / 1024.0);
        else
            return String.format("%.1f MB",
                    bytes / (1024.0 * 1024));
    }
}