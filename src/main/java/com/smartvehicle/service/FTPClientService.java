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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

    private FTPClient ftpClient;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 5000; // Retry delay in ms
    private static final int KEEP_ALIVE_INTERVAL_MS = 30000; // Keep-alive interval (30 seconds)
    private final Lock lock = new ReentrantLock(); // To ensure thread safety

    @PostConstruct
    public void init() throws IOException {
        ftpClient = new FTPClient();
        ftpClient.setDefaultTimeout(60000); // 60 seconds timeout
        ftpClient.setConnectTimeout(60000); // 60 seconds connect timeout
        ftpClient.setDataTimeout(60000); // 60 seconds data timeout

        connectToFtpServer();
    }

    @PreDestroy
    public void destroy() {
        if (ftpClient != null && ftpClient.isConnected()) {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
                log.debug("FTP connection closed.");
            } catch (IOException e) {
                log.error("Error while closing FTP connection", e);
            }
        }
    }

    private void connectToFtpServer() throws IOException {
        int retryCount = 0;
        while (retryCount < MAX_RETRIES) {
            try {
                log.debug("Attempting to connect to FTP server...");
                ftpClient.connect(host, port);
                if (ftpClient.login(username, password)) {
                    ftpClient.enterLocalPassiveMode();
                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                    log.debug("***** FTP connection established ******");
                    return;
                } else {
                    throw new IOException("FTP login failed.");
                }
            } catch (FTPConnectionClosedException e) {
                retryCount++;
                log.warn("FTP connection closed. Retrying... Attempt: " + retryCount);
                if (retryCount >= MAX_RETRIES) {
                    throw e;  // Throw after max retries
                }
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            } catch (IOException e) {
                log.error("Error connecting to FTP server: " + e.getMessage(), e);
                throw new IOException("Error connecting to FTP server: " + e.getMessage(), e);
            }
        }
    }

    @Scheduled(fixedDelay = KEEP_ALIVE_INTERVAL_MS) // Send a NOOP command every 30 seconds
    public void keepAlive() {
        lock.lock(); // Ensure thread-safety
        try {
            if (ftpClient != null && ftpClient.isConnected()) {
                try {
                    ftpClient.sendNoOp();
                    log.debug("Sent NOOP to keep FTP connection alive.");
                } catch (IOException e) {
                    log.error("Error sending NOOP command: " + e.getMessage(), e);
                    reconnect();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private void reconnect() {
        log.warn("Reconnecting to FTP server...");
        try {
            destroy(); // Close any existing connection
            connectToFtpServer(); // Try to reconnect
        } catch (IOException e) {
            log.error("Failed to reconnect to FTP server.", e);
        }
    }

    public byte[] readFile(String imageName) throws IOException {
        int retryCount = 0;
        while (retryCount < MAX_RETRIES) {
            try {
                String remoteFilePath = remoteDir.trim() + "/" + imageName.trim();
                try (InputStream inputStream = ftpClient.retrieveFileStream(remoteFilePath)) {
                    if (inputStream == null) {
                        throw new IOException("Could not retrieve file. Check file path or permissions.");
                    }
                    log.debug("Reading file from inputStream");

                    BufferedImage image = ImageIO.read(inputStream);
                    if (image == null) {
                        throw new IOException("Failed to read the image from the InputStream.");
                    }

                    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                        ImageIO.write(image, "jpg", outputStream);
                        if (!ftpClient.completePendingCommand()) {
                            throw new IOException("Failed to complete FTP file transfer.");
                        }
                        return outputStream.toByteArray();
                    }
                }
            } catch (FTPConnectionClosedException e) {
                retryCount++;
                log.warn("FTP connection closed. Retrying... Attempt: " + retryCount);
                reconnect(); // Reconnect to the server before retrying
            } catch (IOException e) {
                retryCount++;
                log.error("Error reading file: " + e.getMessage(), e);
                if (retryCount >= MAX_RETRIES) {
                    throw e;
                }
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw new IOException("Failed to read file after " + MAX_RETRIES + " attempts.");
    }
}
