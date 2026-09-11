package org.example.librarymanagementsystem.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.librarymanagementsystem.Service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;

@RestController
@RequestMapping("/api/files")
@Tag(name = "Files",
        description = "File serving endpoints")
@Slf4j
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    // ─── Serve cover image ─────────────────────────
    @Operation(
            summary = "Get book cover image",
            description = "Returns the book cover image file"
    )
    @GetMapping("/covers/{fileName:.+}")
    public ResponseEntity<Resource> getCoverImage(
            @PathVariable String fileName) {

        log.debug("Serving file: {}", fileName);

        try {
            Path filePath =
                    fileStorageService.loadFile(fileName);

            Resource resource =
                    new UrlResource(
                            filePath.toUri());

            if (!resource.exists()
                    || !resource.isReadable()) {
                return ResponseEntity
                        .notFound().build();
            }

            // detect content type
            String contentType =
                    determineContentType(fileName);

            return ResponseEntity.ok()
                    .contentType(
                            MediaType.parseMediaType(contentType))
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" +
                                    resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("File serve error: {}",
                    e.getMessage());
            return ResponseEntity
                    .notFound().build();
        }
    }

    // ─── Determine content type ────────────────────
    private String determineContentType(
            String fileName) {

        String lower = fileName.toLowerCase();

        if (lower.endsWith(".jpg")
                || lower.endsWith(".jpeg"))
            return "image/jpeg";
        else if (lower.endsWith(".png"))
            return "image/png";
        else if (lower.endsWith(".gif"))
            return "image/gif";
        else if (lower.endsWith(".webp"))
            return "image/webp";
        else
            return "application/octet-stream";
    }
}