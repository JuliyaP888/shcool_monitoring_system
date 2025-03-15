package com.prishedko.dto;

import java.util.ArrayList;
import java.util.List;

public class StudentDTO {
    private Long id;
    private String name;
    private Long schoolId;
    private List<Long> courseIds = new ArrayList<>();

    public StudentDTO() {
    }

    public StudentDTO(Long id, String name, Long schoolId, List<Long> courseIds) {
        this.id = id;
        this.name = name;
        this.schoolId = schoolId;
        this.courseIds = courseIds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }

    public List<Long> getCourseIds() {
        return courseIds;
    }

    public void setCourseIds(List<Long> courseIds) {
        this.courseIds = courseIds;
    }
}