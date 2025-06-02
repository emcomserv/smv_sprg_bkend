package com.smartvehicle.service;

import com.smartvehicle.entity.Image;
import com.smartvehicle.payload.response.ImageResponseDTO;
import com.smartvehicle.repository.ImageRepository;
import com.smartvehicle.socket.MediaSocketClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ImageService {

    @Autowired
    private ImageRepository imageRepository;

    public Image saveImage(Image image) {
        return imageRepository.save(image);
    }

    public Optional<Image> getImageById(Long id) {
        return imageRepository.findById(id);
    }

    public List<ImageResponseDTO> getImagesByStudentId(String studentId) {
        List<Image> images = imageRepository.findByStudentId(studentId);
        List<ImageResponseDTO> dtos = new ArrayList<>();
        for (Image image : images) {
            // Reconstruct ftpPath since it's not stored in the database
            String ftpFilePath = String.format("/upload/%s/%s/Default/%s_%s_img1.%s",
                    image.getSchoolId(), image.getStudentId(), image.getSchoolId(), image.getStudentId(), image.getFormat());
            dtos.add(new ImageResponseDTO(
                    image.getId(),
                    image.getSchoolId(),
                    image.getStudentId(),
                    image.getFormat(),
                    image.getVehNum(),
                    image.getCommand(),
                    image.getDetectType(),
                    image.getReserve(),
                    image.getBytecode(),
                    ftpFilePath // Use reconstructed ftpPath
            ));
        }
        return dtos;
    }

    public List<Image> getAllImages() {
        return imageRepository.findAll();
    }

    public byte[] sendImageToPython(Image image, String ftpPath) throws IOException {
        List<String> formats = List.of(image.getFormat());
        List<String> filePaths = List.of(ftpPath);
        List<String> vehNums = List.of(image.getVehNum() != null ? image.getVehNum() : "");
        List<Integer> detectTypes = List.of(image.getDetectType() != null ? image.getDetectType() : -1);
        List<String> reserves = List.of(image.getReserve() != null ? image.getReserve() : "");

        return MediaSocketClient.sendFilePathsToPython(
                image.getId(), image.getSchoolId(), image.getStudentId(), formats, filePaths, vehNums, detectTypes, reserves
        );
    }

    public byte[] sendGroupedImagesToPython(String studentId, List<String> ftpPaths) throws IOException {
        List<ImageResponseDTO> images = getImagesByStudentId(studentId);
        if (images.isEmpty()) {
            throw new IOException("No images found for studentId: " + studentId);
        }

        List<String> formats = new ArrayList<>();
        List<String> filePaths = new ArrayList<>();
        List<String> vehNums = new ArrayList<>();
        List<Integer> detectTypes = new ArrayList<>();
        List<String> reserves = new ArrayList<>();

        for (int i = 0; i < images.size(); i++) {
            ImageResponseDTO image = images.get(i);
            formats.add(image.getFormat());
            filePaths.add(ftpPaths.get(i)); // Use provided ftpPaths
            vehNums.add(image.getVehNum() != null ? image.getVehNum() : "");
            detectTypes.add(image.getDetectType() != null ? image.getDetectType() : -1);
            reserves.add(image.getReserve() != null ? image.getReserve() : "");
        }

        return MediaSocketClient.sendFilePathsToPython(
                images.get(0).getId(), images.get(0).getSchoolId(), studentId, formats, filePaths, vehNums, detectTypes, reserves
        );
    }

    public void saveProcessedFile(String schoolId, String studentId, Long imageId, String format, byte[] processedBytes) {
        // Placeholder: Save processed file to database or FTP
    }

    public void saveProcessedResponse(String studentId, byte[] responseBytes) {
        // Placeholder: Save Python server response to database or file
    }
}