package tn.esprit.control;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/user/images")
@Component
public class ImageController {

    private static final long MAX_IMAGE_SIZE = 5_242_880; // 5MB
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/user/uploads/";
    private static final String IMAGE_URL_PREFIX = "/api/user/images/";

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            Path file = Paths.get(UPLOAD_DIR).resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(file);
                if (contentType == null) {
                    contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
                }

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, contentType)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public String handleImageUpload(MultipartFile image, String currentImageUrl) throws IOException {
        validateImageFile(image);

        if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
            deleteOldImage(currentImageUrl);
        }

        String fileName = saveImageFile(image);
        return IMAGE_URL_PREFIX + fileName;
    }

    private String saveImageFile(MultipartFile image) throws IOException {
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(image.getOriginalFilename()));
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID() + fileExtension;
        Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);
        Path filePath = uploadPath.resolve(fileName).normalize();

        if (!filePath.getParent().equals(uploadPath)) {
            throw new IOException("Invalid file path");
        }

        image.transferTo(filePath);
        return fileName;
    }

    private void validateImageFile(MultipartFile image) {
        if (!Objects.requireNonNull(image.getContentType()).startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }
        if (image.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed (5MB)");
        }
    }

    private void deleteOldImage(String imageUrl) throws IOException {
        String oldFileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        Path oldImagePath = Paths.get(UPLOAD_DIR).resolve(oldFileName);
        Files.deleteIfExists(oldImagePath);
    }
}
