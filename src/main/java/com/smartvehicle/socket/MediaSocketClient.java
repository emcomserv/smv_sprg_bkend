package com.smartvehicle.socket;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

@Slf4j
public class MediaSocketClient {

    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int PORT = 5001;
    private static final int TIMEOUT = 10000; // 2 seconds

    // Deprecated: Only needed if sending raw image bytes
    public static byte[] sendImageDataToPython(Long id, String schoolId, String studentId, String format, byte[] imageData,
                                               String vehNum, Integer detectType, String reserve) throws IOException {

        log.warn("sendImageDataToPython is deprecated — image data should now be sent via FTP only.");
        throw new UnsupportedOperationException("Direct image transfer is disabled. Use FTP and send paths.");
    }

    public static byte[] sendJsonToPython(String jsonString) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(SERVER_ADDRESS, PORT), TIMEOUT);

            try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                 DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()))) {

                dos.writeUTF(jsonString);                 // Send JSON string
                dos.writeInt(jsonString.length());        // Send length
                dos.flush();

                int size = dis.readInt();                 // Response length
                byte[] receivedBytes = new byte[size];
                dis.readFully(receivedBytes);             // Full response

                return receivedBytes;
            }
        }
    }

    public static byte[] sendFilePathsToPython(Long id, String schoolId, String studentId,
                                               List<String> formats, List<String> filePaths,
                                               List<String> vehNums, List<Integer> detectTypes,
                                               List<String> reserves) throws IOException {
        if (filePaths == null || filePaths.size() != 4 || formats == null || formats.size() != 4 ||
                vehNums == null || vehNums.size() != 4 || detectTypes == null || detectTypes.size() != 4 ||
                reserves == null || reserves.size() != 4) {
            throw new IllegalArgumentException("Exactly 4 file paths and corresponding metadata must be provided.");
        }

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(SERVER_ADDRESS, PORT), TIMEOUT);

            try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                 DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()))) {

                long startTime = System.currentTimeMillis();

                // Send number of files
                dos.writeInt(4);

                // Send common metadata
                dos.writeUTF(id.toString());
                dos.writeUTF(schoolId != null ? schoolId : "");
                dos.writeUTF(studentId != null ? studentId : "");

                // Send metadata for each file
                for (int i = 0; i < 4; i++) {
                    log.info("Sending file {}: {}", i + 1, filePaths.get(i));
                    dos.writeUTF(formats.get(i) != null ? formats.get(i) : "");
                    dos.writeUTF(filePaths.get(i) != null ? filePaths.get(i) : "");
                    dos.writeUTF(vehNums.get(i) != null ? vehNums.get(i) : "");
                    dos.writeInt(detectTypes.get(i) != null ? detectTypes.get(i) : -1);
                    dos.writeUTF(reserves.get(i) != null ? reserves.get(i) : "");
                }

                dos.flush();

                // Receive response
                int size = dis.readInt();
                byte[] receivedBytes = new byte[size];
                dis.readFully(receivedBytes);

                long duration = System.currentTimeMillis() - startTime;
                log.info("Sent {} file paths to Python server in {} ms, received {} bytes", filePaths.size(), duration, size);

                return receivedBytes;
            }
        }
    }
}
