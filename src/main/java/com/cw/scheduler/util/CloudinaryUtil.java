package com.cw.scheduler.util;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryUtil {

    private final Cloudinary cloudinary;

    private static final String BASE_FOLDER = "service_provider_uploads/";
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of("image/jpeg", "image/png", "image/webp");
    private static final String PDF_TYPE = "application/pdf";

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;  // 5 MB
    private static final long MAX_PDF_SIZE = 10 * 1024 * 1024;   // 10 MB

    public Map<String, Object> uploadFile(MultipartFile file, String folderName) throws IOException {
        validateFile(file);

        File tempFile = File.createTempFile("upload-", file.getOriginalFilename());
        file.transferTo(tempFile);

        String contentType = file.getContentType();
        String resourceType = ALLOWED_IMAGE_TYPES.contains(contentType) ? "image" : "raw";

        @SuppressWarnings("unchecked")
        Map<String, Object> options = ObjectUtils.asMap(
                "resource_type", resourceType,
                "folder", BASE_FOLDER + folderName + "/"
        );

        log.info("Uploading {} to Cloudinary (type: {})", file.getOriginalFilename(), resourceType);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(tempFile, options);

            if (!result.containsKey("public_id") || !result.containsKey("secure_url")) {
                throw new IOException("Cloudinary response missing expected keys.");
            }

            return result;
        } catch (IOException e) {
            log.error("Failed to upload {}: {}", file.getOriginalFilename(), e.getMessage());
            throw e;
        } finally {
            if (!tempFile.delete()) {
                log.warn("Could not delete temp file: {}", tempFile.getAbsolutePath());
            }
        }
    }

    public void deleteFile(String publicId, String resourceType) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, Object> options = ObjectUtils.asMap("resource_type", resourceType);
        cloudinary.uploader().destroy(publicId, options);
    }

    private void validateFile(MultipartFile file) {
        String contentType = file.getContentType();

        if (contentType == null || (!ALLOWED_IMAGE_TYPES.contains(contentType) && !contentType.equals(PDF_TYPE))) {
            throw new IllegalArgumentException("Only JPG, PNG, WEBP images or PDF files are allowed.");
        }

        long size = file.getSize();
        if ((ALLOWED_IMAGE_TYPES.contains(contentType) && size > MAX_IMAGE_SIZE)
                || (contentType.equals(PDF_TYPE) && size > MAX_PDF_SIZE)) {
            throw new IllegalArgumentException("File too large. Max: 5MB for images, 10MB for PDFs.");
        }
    }

    public String getSecureUrl(Map<String, Object> result) {
        return result.get("secure_url").toString();
    }

    public String getPublicId(Map<String, Object> result) {
        return result.get("public_id").toString();
    }

}
