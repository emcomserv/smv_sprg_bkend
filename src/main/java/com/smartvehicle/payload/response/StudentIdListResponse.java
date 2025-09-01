package com.smartvehicle.payload.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class StudentIdListResponse {
    private String count;
    private List<String> studentList;
    private Map<String, String> imagesBase64;

    public StudentIdListResponse(String count, List<String> studentList) {
        this.count = count;
        this.studentList = studentList;
    }

    public StudentIdListResponse(String count, List<String> studentList, Map<String, String> imagesBase64) {
        this.count = count;
        this.studentList = studentList;
        this.imagesBase64 = imagesBase64;
    }
}


