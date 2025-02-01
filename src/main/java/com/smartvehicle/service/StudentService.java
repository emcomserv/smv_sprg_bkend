package com.smartvehicle.service;

import com.smartvehicle.entity.Student;
import com.smartvehicle.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {
    @Autowired
    private StudentRepository studentRepository;

    public List<Student> findStudentsByParentId(Long parentId) {
        return studentRepository.findAllByParent_Id(parentId);
    }
}
