package com.smartvehicle.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@Service
public class HardwareTcpServerService {
    private static final int PORT = 5000;

    @Autowired
    private GpsBroadcastService gpsBroadcastService;

    @Autowired
    private ObjectMapper objectMapper; // Jackson ObjectMapper

    @PostConstruct
    public void startServer() {
        Thread serverThread = new Thread(this::runServer, "HardwareTcpServer-Thread");
        serverThread.setDaemon(true);
        serverThread.start();
    }

    private void runServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("✅ Hardware TCP Server listening on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("🔌 Client connected: " + clientSocket.getInetAddress());
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket socket) {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("📥 Received: " + line);

                if (isValidJson(line)) {
                    gpsBroadcastService.broadcastGpsData(line); // Still calls your service
                } else {
                    System.err.println("❌ Invalid JSON received: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("❗ Error handling client: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json); // Jackson validation
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}