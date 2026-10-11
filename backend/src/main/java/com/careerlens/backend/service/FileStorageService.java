package com.careerlens.backend.service;

import org.springframework.core.io.Resource;

import java.io.InputStream;

public interface FileStorageService {
    String storeFile(InputStream inputStream, String originalFilename, String storedFilename);
    Resource loadFileAsResource(String storagePath);
    void deleteFile(String storagePath);
}
