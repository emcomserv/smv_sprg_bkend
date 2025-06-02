package com.smartvehicle.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.SocketException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
public class FTPClientService {

    @Value("${ftp.host}")
    private String host;

    @Value("${ftp.port}")
    private int port;

    @Value("${ftp.username}")
    private String username;

    @Value("${ftp.password}")
    private String password;

    @Value("${ftp.remoteDir}")
    private String remoteDir;

    @Value("${ftp.buffer.size:16384}")
    private int bufferSize;

    @Value("${ftp.use.chmod:false}")
    private boolean useChmod;

    @Value("${ftp.use.passive:true}")
    private boolean usePassive;

    @Value("${ftp.timeout:60000}")
    private int uploadTimeout;

    private FTPClient ftpClient;
    private final Set<String> createdDirectories = new HashSet<>();
    private static final int MAX_RETRIES = 3;
    private static final int RESPONSE_TIMEOUT = 10000; // 60 seconds

    @PostConstruct
    public void init() throws IOException {
        ftpClient = new FTPClient();
        ftpClient.setDefaultTimeout(uploadTimeout);
        ftpClient.setConnectTimeout(uploadTimeout);
        ftpClient.setDataTimeout(uploadTimeout);
        ftpClient.setBufferSize(bufferSize);
        try {
            long startTime = System.currentTimeMillis();
            ftpClient.connect(host, port);
            long connectTime = System.currentTimeMillis() - startTime;
            ftpClient.login(username, password);
            ftpClient.enterLocalPassiveMode();
            log.debug(Instant.now() + " - Using passive mode (ignoring ftp.use.passive={})", usePassive);
            if (!usePassive) {
                log.warn(Instant.now() + " - ftp.use.passive=false is ignored; using passive mode to avoid connection issues");
            }
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setControlKeepAliveTimeout(5);
            log.info(Instant.now() + " - FTP connection established for user: {} in {} ms", username, connectTime);
        } catch (IOException e) {
            log.error(Instant.now() + " - Failed to initialize FTP connection", e);
            throw e;
        }
    }

    @PreDestroy
    public void destroy() {
        if (ftpClient != null && ftpClient.isConnected()) {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
                log.debug(Instant.now() + " - FTP connection closed.");
            } catch (IOException e) {
                log.error(Instant.now() + " - Error while closing FTP connection", e);
            }
        }
    }

    @Scheduled(fixedDelay = 5000)
    public void keepAlive() throws IOException {
        if (ftpClient != null && ftpClient.isConnected()) {
            ftpClient.sendNoOp();
            log.debug(Instant.now() + " - Sent NOOP to keep FTP connection alive");
        }
    }

    public byte[] readFile(String imageName) throws IOException {
        int retryCount = 0;
        while (retryCount < MAX_RETRIES) {
            try {
                String remoteFilePath = remoteDir.trim() + "/" + imageName.trim();
                log.debug(Instant.now() + " - Reading file from inputStream: {}", remoteFilePath);
                try (InputStream inputStream = ftpClient.retrieveFileStream(remoteFilePath)) {
                    if (inputStream == null) {
                        throw new IOException("Could not retrieve file. Check file path or permissions.");
                    }

                    BufferedImage image = ImageIO.read(inputStream);
                    if (image == null) {
                        throw new IOException("Failed to read the image from the InputStream.");
                    }

                    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                        ImageIO.write(image, "jpg", outputStream);
                        if (!ftpClient.completePendingCommand()) {
                            throw new IOException("Failed to complete FTP file transfer.");
                        }
                        log.debug(Instant.now() + " - Successfully read file: {}", remoteFilePath);
                        return outputStream.toByteArray();
                    }
                }
            } catch (FTPConnectionClosedException e) {
                retryCount++;
                log.warn(Instant.now() + " - FTP connection closed. Retrying... Attempt: {}/{}", retryCount, MAX_RETRIES);
                init();
            }
        }
        throw new IOException("Failed to read file after " + MAX_RETRIES + " attempts.");
    }

    public boolean createDirectory(String dirPath) throws IOException {
        int retryCount = 0;
        String normalizedPath = dirPath.trim();
        if (normalizedPath.startsWith("/")) {
            normalizedPath = normalizedPath.substring(1);
        }
        if (normalizedPath.isEmpty()) {
            return true;
        }

        // Reinitialize the FTP connection to reset any session limits
        if (ftpClient.isConnected()) {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
                log.debug(Instant.now() + " - Closed existing FTP connection to reset session");
            } catch (IOException e) {
                log.warn(Instant.now() + " - Error closing FTP connection before reinitialization", e);
            }
        }
        init(); // Reinitialize the connection
        log.debug(Instant.now() + " - Reinitialized FTP connection for directory creation");

        // Temporarily bypass the cache for debugging
        synchronized (createdDirectories) {
            if (createdDirectories.contains(normalizedPath)) {
                log.debug(Instant.now() + " - Directory {} found in cache, verifying on server", normalizedPath);
                if (ftpClient.changeWorkingDirectory("/" + normalizedPath)) {
                    log.debug(Instant.now() + " - Directory {} confirmed to exist on server", normalizedPath);
                    return true;
                } else {
                    log.warn(Instant.now() + " - Directory {} in cache but not on server, removing from cache", normalizedPath);
                    createdDirectories.remove(normalizedPath);
                }
            }
        }

        while (retryCount < MAX_RETRIES) {
            try {
                long startTime = System.currentTimeMillis();
                log.debug(Instant.now() + " - Attempting to create directory: {}", normalizedPath);
                boolean created = ftpClient.makeDirectory(normalizedPath);
                log.debug("makeDirectory reply: {}", ftpClient.getReplyString());
                if (created || ftpClient.changeWorkingDirectory("/" + normalizedPath)) {
                    long duration = System.currentTimeMillis() - startTime;
                    log.debug(Instant.now() + " - Directory {} created or already exists in {} ms", normalizedPath, duration);
                    synchronized (createdDirectories) {
                        createdDirectories.add(normalizedPath);
                    }
                    if (useChmod) {
                        try {
                            String chmodValue = "755";
                            log.debug(Instant.now() + " - Setting permissions to {} for directory: {}", chmodValue, normalizedPath);
                            ftpClient.sendSiteCommand("CHMOD " + chmodValue + " " + normalizedPath);
                        } catch (IOException e) {
                            log.warn(Instant.now() + " - Failed to set permissions for directory: {}", normalizedPath, e);
                        }
                    }
                    log.info(Instant.now() + " - Successfully created or accessed directory: {}", dirPath);
                    return true;
                } else {
                    log.error(Instant.now() + " - Failed to create directory: {}, FTP reply: {}", normalizedPath, ftpClient.getReplyString());
                    throw new IOException("Failed to create directory: " + normalizedPath);
                }
            } catch (FTPConnectionClosedException e) {
                retryCount++;
                log.warn(Instant.now() + " - FTP connection closed during directory creation. Retrying... Attempt: {}/{}", retryCount, MAX_RETRIES);
                init();
            }
            if (retryCount < MAX_RETRIES) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.warn(Instant.now() + " - Retry delay interrupted", ie);
                }
            }
        }
        log.error(Instant.now() + " - Failed to create directory after {} attempts: {}", MAX_RETRIES, dirPath);
        return false;
    }

    public boolean uploadFile(String remotePath, InputStream inputStream) throws IOException {
        // Check if file already exists before uploading
        if (fileExists(remotePath)) {
            log.info(Instant.now() + " - File {} already exists on FTP server, skipping upload", remotePath);
            return true;
        }

        int retryCount = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[bufferSize];
        int bytesRead;
        long startBuffering = System.currentTimeMillis();
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        byte[] fileData = baos.toByteArray();
        long bufferingTime = System.currentTimeMillis() - startBuffering;
        log.debug(Instant.now() + " - Buffered {} bytes for upload to: {} in {} ms", fileData.length, remotePath, bufferingTime);

        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new ByteArrayInputStream(fileData), bufferSize)) {
            while (retryCount < MAX_RETRIES) {
                try {
                    if (!ftpClient.isConnected() || !ftpClient.sendNoOp()) {
                        log.warn(Instant.now() + " - FTP connection is not active. Reinitializing...");
                        init();
                    }

                    log.info(Instant.now() + " - Uploading file to FTP path: {} ({} bytes), attempt {}/{}", remotePath, fileData.length, retryCount + 1, MAX_RETRIES);
                    long startTime = System.currentTimeMillis();

                    ftpClient.setSendBufferSize(bufferSize);
                    ftpClient.setReceiveBufferSize(bufferSize);

                    // Start data transfer
                    long dataTransferStart = System.currentTimeMillis();
                    boolean success = ftpClient.storeFile(remotePath, bufferedInputStream);
                    long dataTransferDuration = System.currentTimeMillis() - dataTransferStart;
                    log.info(Instant.now() + " - Data transfer completed for: {} in {} ms", remotePath, dataTransferDuration);

                    // Wait for server response with a timeout
                    long responseWaitStart = System.currentTimeMillis();
                    ftpClient.setSoTimeout(RESPONSE_TIMEOUT);
                    boolean completed = ftpClient.completePendingCommand();
                    long responseWaitDuration = System.currentTimeMillis() - responseWaitStart;
                    log.info(Instant.now() + " - Waited {} ms for server response (226 Transfer complete)", responseWaitDuration);

                    long totalDuration = System.currentTimeMillis() - startTime;
                    double throughput = dataTransferDuration > 0 ? (fileData.length / 1024.0) / (dataTransferDuration / 1000.0) : 0;

                    if (!completed) {
                        log.warn(Instant.now() + " - Failed to receive server response after {} ms, control connection state: {}, FTP reply: {}",
                                totalDuration, ftpClient.isConnected() ? "connected" : "disconnected", ftpClient.getReplyString());
                        // Immediate post-timeout file existence check
                        boolean exists = fileExists(remotePath);
                        log.info(Instant.now() + " - Post-timeout file existence check for {} returned: {}", remotePath, exists);
                        if (exists) {
                            log.info(Instant.now() + " - File {} exists on server despite timeout, treating as success", remotePath);
                            return true;
                        }
                        throw new IOException("Failed to complete FTP file transfer: server did not send completion response within " + RESPONSE_TIMEOUT + " ms");
                    }

                    if (success) {
                        if (useChmod) {
                            try {
                                String chmodValue = "644";
                                log.debug(Instant.now() + " - Setting permissions to {} for file: {}", chmodValue, remotePath);
                                ftpClient.sendSiteCommand("CHMOD " + chmodValue + " " + remotePath);
                            } catch (IOException e) {
                                log.warn(Instant.now() + " - Failed to set permissions for file: {}", remotePath, e);
                            }
                        }
                        log.info(Instant.now() + " - File uploaded successfully to: {} in {} ms (data transfer: {} ms, response wait: {} ms, throughput: {} KB/s)",
                                remotePath, totalDuration, dataTransferDuration, responseWaitDuration, String.format("%.2f", throughput));
                        return true;
                    } else {
                        log.error(Instant.now() + " - FTP upload failed for: {} after {} ms, control connection state: {}, FTP reply: {}",
                                remotePath, totalDuration, ftpClient.isConnected() ? "connected" : "disconnected", ftpClient.getReplyString());
                        throw new IOException("FTP upload failed for: " + remotePath);
                    }
                } catch (FTPConnectionClosedException | SocketException e) {
                    retryCount++;
                    log.warn(Instant.now() + " - FTP connection issue during file upload. Retrying... Attempt: {}/{}", retryCount, MAX_RETRIES, e);
                    init();
                } catch (IOException e) {
                    retryCount++;
                    log.warn(Instant.now() + " - Upload failed for: {} after attempt {}/{}, error: {}. Checking file existence before retry...", remotePath, retryCount, MAX_RETRIES, e.getMessage());
                    // Immediate pre-retry file existence check
                    boolean exists = fileExists(remotePath);
                    log.info(Instant.now() + " - Pre-retry file existence check for {} returned: {}", remotePath, exists);
                    if (exists) {
                        log.info(Instant.now() + " - File {} exists on server before retry, treating as success", remotePath);
                        return true;
                    }
                    if (retryCount >= MAX_RETRIES) {
                        log.error(Instant.now() + " - Failed to upload file after {} attempts: {}, error: {}", MAX_RETRIES, remotePath, e.getMessage());
                        // Final existence check before failing
                        boolean finalExists = fileExists(remotePath);
                        log.info(Instant.now() + " - Final file existence check for {} returned: {}", remotePath, finalExists);
                        if (finalExists) {
                            log.info(Instant.now() + " - File {} exists on server after retries, treating as success", remotePath);
                            return true;
                        }
                        throw e;
                    }
                    init();
                } finally {
                    ftpClient.setSoTimeout(0);
                }
                if (retryCount < MAX_RETRIES) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn(Instant.now() + " - Retry delay interrupted", ie);
                    }
                }
            }
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.warn(Instant.now() + " - Failed to close InputStream for: {}", remotePath, e);
            }
        }
        log.error(Instant.now() + " - Failed to upload file after {} attempts: {}", MAX_RETRIES, remotePath);
        return false;
    }

    // Helper method to check if file exists on FTP server
    private boolean fileExists(String remotePath) {
        try {
            ftpClient.setSoTimeout(5000); // Short timeout for this check
            String directory = remotePath.substring(0, remotePath.lastIndexOf('/'));
            String filename = remotePath.substring(remotePath.lastIndexOf('/') + 1);

            // Ensure directory is accessible
            boolean dirAccessible = ftpClient.changeWorkingDirectory(directory);
            log.debug(Instant.now() + " - Changed working directory to {}: {}", directory, dirAccessible ? "success" : "failed, reply: " + ftpClient.getReplyString());
            if (!dirAccessible) {
                log.warn(Instant.now() + " - Directory {} not accessible for file existence check", directory);
            }

            for (int i = 0; i < 2; i++) {
                try {
                    // Use listFiles for reliable listing
                    boolean exists = false;
                    var files = ftpClient.listFiles(directory);
                    for (var file : files) {
                        if (file.getName().equals(filename)) {
                            exists = true;
                            break;
                        }
                    }
                    log.debug(Instant.now() + " - File existence check for {} attempt {}/5 returned: {}, reply: {}, files listed: {}",
                            remotePath, i + 1, exists, ftpClient.getReplyString(), files.length);
                    if (exists) return true;

                    Thread.sleep(4000);
                } catch (IOException e) {
                    log.warn(Instant.now() + " - File existence check attempt {}/5 for {} failed: {}, reply: {}",
                            i + 1, remotePath, e.getMessage(), ftpClient.getReplyString());
                }
            }
            log.warn(Instant.now() + " - File {} not found after 5 existence check attempts", remotePath);
            return false;
        } catch (IOException | InterruptedException e) {
            log.warn(Instant.now() + " - Failed to check if file exists on server: {}", remotePath, e);
            return false;
        } finally {
            try {
                ftpClient.setSoTimeout(0);
                // Reset working directory
                ftpClient.changeWorkingDirectory("/");
            } catch (IOException e) {
                log.warn(Instant.now() + " - Failed to reset FTP state after file existence check", e);
            }
        }
    }

    public boolean uploadZippedFiles(String remotePath, List<byte[]> imageDataList, List<String> fileNames) throws IOException {
        long startTime = System.currentTimeMillis();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (int i = 0; i < imageDataList.size(); i++) {
                ZipEntry entry = new ZipEntry(fileNames.get(i));
                zos.putNextEntry(entry);
                zos.write(imageDataList.get(i));
                zos.closeEntry();
            }
        }
        byte[] zipData = baos.toByteArray();
        long zipTime = System.currentTimeMillis() - startTime;
        log.debug(Instant.now() + " - Created zip file of {} bytes for upload to: {} in {} ms", zipData.length, remotePath, zipTime);

        try (BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(zipData), bufferSize)) {
            return uploadFile(remotePath, bis);
        }
    }
}