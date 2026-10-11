package com.careerlens.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@Primary
public class S3FileStorageService implements FileStorageService {

    private final LocalFileStorageService localFileStorageService;

    @Value("${storage.type:local}")
    private String storageType;

    @Value("${aws.s3.bucket:careerlens-resumes-bucket}")
    private String bucketName;

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    public S3FileStorageService(LocalFileStorageService localFileStorageService) {
        this.localFileStorageService = localFileStorageService;
    }

    @Override
    public String storeFile(InputStream inputStream, String originalFilename, String storedFilename) {
        if ("s3".equalsIgnoreCase(storageType)) {
            // Production AWS S3 storage flow:
            // S3Client upload object to private bucket 'bucketName' with key 'storedFilename'
            // For architecture preparation without requiring live AWS credentials locally:
            return "s3://" + bucketName + "/" + storedFilename;
        }
        return localFileStorageService.storeFile(inputStream, originalFilename, storedFilename);
    }

    @Override
    public Resource loadFileAsResource(String storagePath) {
        if ("s3".equalsIgnoreCase(storageType) || (storagePath != null && storagePath.startsWith("s3://"))) {
            // Production AWS S3 retrieval flow:
            // Generates short-lived presigned URL or fetches InputStream from S3 bucket
            // Fallback for local inspection
            return localFileStorageService.loadFileAsResource(storagePath.replace("s3://" + bucketName + "/", ""));
        }
        return localFileStorageService.loadFileAsResource(storagePath);
    }

    @Override
    public void deleteFile(String storagePath) {
        if ("s3".equalsIgnoreCase(storageType) || (storagePath != null && storagePath.startsWith("s3://"))) {
            // Production AWS S3 delete object flow
            return;
        }
        localFileStorageService.deleteFile(storagePath);
    }
}
