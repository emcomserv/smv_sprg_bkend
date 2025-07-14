package com.smartvehicle.service;

import com.smartvehicle.socket.MediaSocketClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ImageUploadService {

    @Autowired
    private FTPClientService ftpClientService;

    public byte[] uploadImagesAndSendPaths(Long id, String schoolId, String studentId, List<byte[]> images, List<String> formats,
                                           List<String> vehNums, List<Integer> detectTypes, List<String> reserves) throws IOException {
        if (images.size() != 4 || formats.size() != 4 || vehNums.size() != 4 || detectTypes.size() != 4 || reserves.size() != 4) {
            throw new IllegalArgumentException("Exactly 4 images and metadata required");
        }

        // Create directory
        String basePath = String.format("/upload/%s/%s/Default", schoolId, studentId);
        long startTime = System.currentTimeMillis();
        boolean dirCreated = ftpClientService.createDirectory(basePath);
        long dirTime = System.currentTimeMillis() - startTime;
        if (!dirCreated) {
            log.error("Failed to create directory: {}", basePath);
            throw new IOException("Directory creation failed");
        }
        log.info("Created directory: {} in {} ms", basePath, dirTime);

        // Upload images and collect paths
        List<String> filePaths = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            String fileName = String.format("%s_%s_img%d.jpg", schoolId, studentId, i + 1);
            String remotePath = basePath + "/" + fileName;
            try (ByteArrayInputStream bis = new ByteArrayInputStream(images.get(i))) {
                boolean uploaded = ftpClientService.uploadFile(remotePath, bis);
                if (!uploaded) {
                    log.error("Failed to upload image {} to: {}", i + 1, remotePath);
                    throw new IOException("Image upload failed for: " + remotePath);
                }
                filePaths.add(remotePath);
                log.info("Uploaded image {} to: {}", i + 1, remotePath);
            }
        }

        // Send file paths to Python server
        startTime = System.currentTimeMillis();
        byte[] socketResponse = MediaSocketClient.sendFilePathsToPython(
                id, schoolId, studentId, formats, filePaths, vehNums, detectTypes, reserves
        );
        long socketTime = System.currentTimeMillis() - startTime;
        log.info("Sent file paths to Python server in {} ms, response size: {} bytes", socketTime, socketResponse.length);

        return socketResponse;
    }
}