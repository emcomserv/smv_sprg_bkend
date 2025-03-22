package com.smartvehicle.service;

import com.smartvehicle.entity.Student;
import com.smartvehicle.entity.SwipeImage;
import com.smartvehicle.repository.SwipeImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SwipeImageService {
    @Autowired
    private SwipeImageRepository swipeImageRepository;

    public List<SwipeImage> getSwipesByStudentId(String studentId) {
        return swipeImageRepository.findByStudentId(studentId);
    }
}

