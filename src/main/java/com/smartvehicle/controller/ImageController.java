package com.smartvehicle.controller;

import com.smartvehicle.payload.response.ImageResponseDTO;
import com.smartvehicle.entity.Image;
import com.smartvehicle.entity.Student;
import com.smartvehicle.service.FTPClientService;
import com.smartvehicle.service.ImageService;
import com.smartvehicle.socket.MediaSocketClient;
import com.smartvehicle.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/images")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @Autowired
    private FTPClientService ftpService;
    @Autowired
    private StudentRepository studentRepository;

    private static final String DEFAULT_SUBDIR = "Default";
    private static final int REQUIRED_FILE_COUNT = 4;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImages(
            @RequestParam String schoolId,
            @RequestParam String studentId,
            @RequestParam String routeId,
            @RequestParam("file") MultipartFile[] files,
            @RequestParam(required = false) String command,
            @RequestParam(required = false) String devId,
            @RequestParam(required = false) String vehNum,
            @RequestParam(required = false) Integer detectType,
            @RequestParam(required = false) String reserve) {

        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest().body("No files provided");
        }
        if (files.length < 1 || files.length > 4) {
            return ResponseEntity.badRequest().body("You can upload between 1 and 4 files");
        }

        StringBuilder resultMessage = new StringBuilder();

        try {
            // Construct the remote path dynamically using provided schoolId, routeId and studentId
            String basePath = "/upload";
            String schoolPath = basePath + "/" + schoolId;                 // e.g., /upload/AC0F0016
            String routePath = schoolPath + "/" + routeId;                  // e.g., /upload/AC0F0016/RT7F0001
            String studentPath = routePath + "/" + studentId;               // e.g., /upload/AC0F0016/RT7F0001/ST0F0001
            String finalPath = studentPath + "/" + DEFAULT_SUBDIR;          // e.g., /upload/AC0F0016/RT7F0001/ST0F0001/default

            // Create directories on FTP if they don't exist
            try {
                boolean schoolDirCreated = ftpService.createDirectory(schoolPath);
                if (!schoolDirCreated) {
                    String errorMsg = "Failed to create or access directory: " + schoolPath + ". Check FTP permissions and server logs for details.\n";
                    resultMessage.append(errorMsg);
                    System.out.println(Instant.now() + " - " + errorMsg);
                    return ResponseEntity.status(500).body(resultMessage.toString());
                }
                System.out.println(Instant.now() + " - Successfully created/accessed directory: " + schoolPath);
                Thread.sleep(2000); // Add 2-second delay

                boolean routeDirCreated = ftpService.createDirectory(routePath);
                if (!routeDirCreated) {
                    String errorMsg = "Failed to create or access directory: " + routePath + ". Check FTP permissions and server logs for details.\n";
                    resultMessage.append(errorMsg);
                    System.out.println(Instant.now() + " - " + errorMsg);
                    return ResponseEntity.status(500).body(resultMessage.toString());
                }
                System.out.println(Instant.now() + " - Successfully created/accessed directory: " + routePath);
                Thread.sleep(2000); // Add 2-second delay

                boolean studentDirCreated = ftpService.createDirectory(studentPath);
                if (!studentDirCreated) {
                    String errorMsg = "Failed to create or access directory: " + studentPath + ". Check FTP permissions and server logs for details.\n";
                    resultMessage.append(errorMsg);
                    System.out.println(Instant.now() + " - " + errorMsg);
                    return ResponseEntity.status(500).body(resultMessage.toString());
                }
                System.out.println(Instant.now() + " - Successfully created/accessed directory: " + studentPath);
                Thread.sleep(2000); // Add 2-second delay

                boolean defaultDirCreated = ftpService.createDirectory(finalPath);
                if (!defaultDirCreated) {
                    String errorMsg = "Failed to create or access directory: " + finalPath + ". Check FTP permissions and server logs for details.\n";
                    resultMessage.append(errorMsg);
                    System.out.println(Instant.now() + " - " + errorMsg);
                    return ResponseEntity.status(500).body(resultMessage.toString());
                }
                System.out.println(Instant.now() + " - Successfully created/accessed directory: " + finalPath);
            } catch (IOException | InterruptedException e) {
                String errorMsg = "Failed to create directories on FTP: " + e.getMessage() + "\n";
                resultMessage.append(errorMsg);
                System.out.println(Instant.now() + " - " + errorMsg);
                return ResponseEntity.status(500).body(resultMessage.toString());
            }

            // Prepare metadata for Python server
            List<String> formats = new ArrayList<>();
            List<String> filePaths = new ArrayList<>();
            List<String> vehNums = new ArrayList<>();
            List<Integer> detectTypes = new ArrayList<>();
            List<String> reserves = new ArrayList<>();
            List<Image> savedImages = new ArrayList<>();

            // Save all images to the database and FTP
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                if (file.isEmpty()) {
                    resultMessage.append("File ").append(i + 1).append(" is empty.\n");
                    continue;
                }

                try {
                    String originalFilename = file.getOriginalFilename();
                    String format = getExtension(originalFilename);
                    byte[] bytecode = file.getBytes();

                    // Save to database without ftpPath
                    Image image = new Image(schoolId, studentId, bytecode, format, command, devId, vehNum, detectType, reserve);
                    Image savedImage = imageService.saveImage(image);
                    savedImages.add(savedImage);

                    // Upload to FTP
                    String ftpFilename = String.format("%s_%s_img%d.%s", schoolId, studentId, i + 1, format);
                    String ftpFilePath = finalPath + "/" + ftpFilename;  // e.g., /upload/AC0F0016/ST0F0001/Default/AC0F0016_ST0F0001_img1.jpg

                    long startTime = System.currentTimeMillis();
                    long fileSize = file.getSize();
                    System.out.println(Instant.now() + " - Uploading file " + (i + 1) + " of size " + fileSize + " bytes to FTP path: " + ftpFilePath);

                    try (InputStream inputStream = new BufferedInputStream(file.getInputStream())) {
                        boolean uploaded = ftpService.uploadFile(ftpFilePath, inputStream);
                        if (uploaded) {
                            long duration = System.currentTimeMillis() - startTime;
                            System.out.println(Instant.now() + " - File " + (i + 1) + " uploaded successfully to FTP in " + duration + " ms");
                            resultMessage.append("Image ").append(i + 1).append(" uploaded successfully to FTP at ").append(ftpFilePath).append(" and saved to database.\n");
                            // Add metadata for Python server
                            formats.add(format);
                            filePaths.add(ftpFilePath);
                            vehNums.add(vehNum != null ? vehNum : "");
                            detectTypes.add(detectType != null ? detectType : -1);
                            reserves.add(reserve != null ? reserve : "");
                        } else {
                            resultMessage.append("FTP upload failed for file ").append(i + 1).append(" (database save successful).\n");
                        }
                    }

                } catch (IOException e) {
                    resultMessage.append("Upload failed for file ").append(i + 1).append(": ").append(e.getMessage()).append("\n");
                }
            }

            // Send FTP paths to Python server if all uploads succeeded
            if (filePaths.size() == files.length) {
                try {
                    long startTime = System.currentTimeMillis();
                    System.out.println(Instant.now() + " - Sending file paths to Python server: " + filePaths);
                    byte[] responseBytes = MediaSocketClient.sendFilePathsToPython(
                            savedImages.get(0).getId(), schoolId, studentId, formats, filePaths, vehNums, detectTypes, reserves
                    );
                    long duration = System.currentTimeMillis() - startTime;
                    resultMessage.append("Sent ").append(filePaths.size()).append(" file paths to Python server in ").append(duration).append(" ms, response size: ")
                            .append(responseBytes.length).append(" bytes\n");
                    imageService.saveProcessedResponse(studentId, responseBytes);
                } catch (IOException e) {
                    resultMessage.append("Failed to send file paths to Python server: ").append(e.getMessage()).append("\n");
                }
            } else {
                resultMessage.append("Some files failed to upload to FTP. Uploaded: ")
                        .append(filePaths.size()).append("/").append(files.length).append(".\n");
            }

        } catch (Exception e) {
            resultMessage.append("Operation failed: ").append(e.getMessage()).append("\n");
        }

        return ResponseEntity.ok(resultMessage.toString());
    }

    @GetMapping("/student/{studentId}/full")
    public ResponseEntity<List<ImageResponseDTO>> getFullImagesByStudentId(@PathVariable String studentId) {
        List<ImageResponseDTO> images = imageService.getImagesByStudentId(studentId);
        if (images.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(images);
    }

    @GetMapping("/student/{studentId}/file/{imageId}")
    public ResponseEntity<byte[]> getImageFileByStudentIdAndImageId(@PathVariable String studentId, @PathVariable Long imageId) {
        Optional<Image> imageOpt = imageService.getImageById(imageId);
        if (imageOpt.isEmpty() || !imageOpt.get().getStudentId().equals(studentId)) {
            return ResponseEntity.notFound().build();
        }

        Image image = imageOpt.get();
        byte[] imageData = image.getBytecode();
        String format = image.getFormat();

        String contentType = getContentType(format);

        return ResponseEntity.ok()
                .header("Content-Type", contentType)
                .header("Content-Disposition", "attachment; filename=\"image." + format + "\"")
                .body(imageData);
    }

    @GetMapping
    public List<Image> getAllImages() {
        return imageService.getAllImages();
    }

    @PostMapping("/send-to-python")
    public ResponseEntity<String> sendImageToPython(@RequestParam Long id) {
        Optional<Image> optionalImage = imageService.getImageById(id);
        if (optionalImage.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Image image = optionalImage.get();
        try {
            // Reconstruct ftpPath since it's not stored in the database
            String ftpFilePath = String.format("/upload/%s/%s/Default/%s_%s_img1.%s",
                    image.getSchoolId(), image.getStudentId(), image.getSchoolId(), image.getStudentId(), image.getFormat());
            System.out.println(Instant.now() + " - Sending file path to Python server: " + ftpFilePath);
            byte[] responseBytes = imageService.sendImageToPython(image, ftpFilePath);
            return ResponseEntity.ok("Image sent to Python server successfully!");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to send image to Python server: " + e.getMessage());
        }
    }

    @PostMapping("/process")
    public ResponseEntity<String> processImageViaPython(@RequestParam Long id) {
        Optional<Image> optionalImage = imageService.getImageById(id);
        if (optionalImage.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Image image = optionalImage.get();
        try {
            // Reconstruct ftpPath since it's not stored in the database
            // If you have routeId available for stored images, include it; otherwise this remains legacy
            String ftpFilePath = String.format("/upload/%s/%s/Default/%s_%s_img1.%s",
                    image.getSchoolId(), image.getStudentId(), image.getSchoolId(), image.getStudentId(), image.getFormat());
            System.out.println(Instant.now() + " - Sending file path to Python server: " + ftpFilePath);
            byte[] responseBytes = imageService.sendImageToPython(image, ftpFilePath);
            imageService.saveProcessedFile(image.getSchoolId(), image.getStudentId(), image.getId(), image.getFormat(), responseBytes);
            return ResponseEntity.ok("Processed file saved!");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Processing failed: " + e.getMessage());
        }
    }

    @PostMapping("/send-grouped-to-python")
    public ResponseEntity<String> sendGroupedImagesToPython(@RequestParam String studentId) {
        List<ImageResponseDTO> images = imageService.getImagesByStudentId(studentId);
        if (images.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (images.size() != REQUIRED_FILE_COUNT) {
            return ResponseEntity.badRequest().body("Exactly " + REQUIRED_FILE_COUNT + " images must be available for studentId: " + studentId);
        }

        try {
            List<String> ftpPaths = new ArrayList<>();
            for (ImageResponseDTO image : images) {
                // Reconstruct ftpPath since it's not stored in the database
                // If routeId is tracked per image, insert it between schoolId and studentId in the path
                String ftpFilePath = String.format("/upload/%s/%s/Default/%s_%s_img%d.%s",
                        image.getSchoolId(), image.getStudentId(), image.getSchoolId(), image.getStudentId(),
                        images.indexOf(image) + 1, image.getFormat());
                ftpPaths.add(ftpFilePath);
            }
            System.out.println(Instant.now() + " - Sending file paths to Python server: " + ftpPaths);
            byte[] responseBytes = imageService.sendGroupedImagesToPython(studentId, ftpPaths);
            return ResponseEntity.ok("Grouped images sent to Python server successfully!");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to send grouped images to Python server: " + e.getMessage());
        }
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex != -1) ? filename.substring(dotIndex + 1) : "jpeg";
    }

    private String getContentType(String format) {
        return switch (format.toLowerCase()) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "mp4" -> "video/mp4";
            default -> "application/octet-stream";
        };
    }
}