package com.careerlens.backend.service;

import com.careerlens.backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path fileStorageLocation;

    public LocalFileStorageService(@Value("${file.upload-dir:./uploads/resumes}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create local upload directory.", ex);
        }
    }

    @Override
    public String storeFile(InputStream inputStream, String originalFilename, String storedFilename) {
        Path targetLocation = this.fileStorageLocation.resolve(storedFilename);
        try {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return targetLocation.toString();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store file " + originalFilename, ex);
        }
    }

    @Override
    public Resource loadFileAsResource(String storagePath) {
        try {
            Path filePath = Paths.get(storagePath).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found on local storage.");
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File path is invalid.", ex);
        }
    }

    @Override
    public void deleteFile(String storagePath) {
        if (storagePath != null) {
            try {
                Files.deleteIfExists(Paths.get(storagePath));
            } catch (Exception ignored) {}
        }
    }
}
