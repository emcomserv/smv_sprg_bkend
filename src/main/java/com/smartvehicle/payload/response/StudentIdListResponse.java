package com.smartvehicle.payload.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class StudentIdListResponse {
    private String count;
    private List<String> studentList;

    public StudentIdListResponse(String count, List<String> studentList) {
        this.count = count;
        this.studentList = studentList;
    }
}


