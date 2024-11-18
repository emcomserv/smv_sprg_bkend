package com.smart_vehicle.Service;

import com.smart_vehicle.models.Student;
import com.smart_vehicle.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {
    @Autowired
    private StudentRepository studentRepository;

    public List<Student> findStudentsByParentId(String parentId) {
        // Replace with appropriate logic to fetch students based on parent's username
        return studentRepository.findByParentId(parentId);
    }
}
