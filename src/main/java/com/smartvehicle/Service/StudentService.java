package com.smartvehicle.Service;

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
        return studentRepository.findByParent_Id(parentId);
    }
}
