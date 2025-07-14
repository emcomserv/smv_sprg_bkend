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

    public List<Student> findStudentsBySchoolId(String schoolId) {

        return studentRepository.findBySchool_Id(schoolId);
    }

    public boolean isValidStudent(String input) {
        try {
            String[] parts = input.split("-");
            if (parts.length < 3) {
                return false;
            }

            //Long routeId = Long.parseLong(parts[0].replaceAll("[^0-9]", ""));
            //Long schoolId = Long.parseLong(parts[1].replaceAll("[^0-9]", ""));
            String studentId = parts[2];
            studentId=studentId.substring(3); // Student ID after "BNG"

            //Student student = studentRepository.findByRouteSchoolAndStudentId(routeId, schoolId, studentId);
            Student student = studentRepository.findByRouteSchoolAndStudentId(studentId);
            return student != null;
        } catch (Exception e) {
            return false;
        }
    }
}
