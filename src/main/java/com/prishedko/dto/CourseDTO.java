package com.prishedko.dto;

import java.util.ArrayList;
import java.util.List;

public class CourseDTO {
    private Long id;
    private String name;
    private List<Long> teacherIds = new ArrayList<>();
    private List<Long> studentIds = new ArrayList<>();

    public CourseDTO() {

    }

    public CourseDTO(Long id, String name, List<Long> teacherIds, List<Long> studentIds) {
        this.id = id;
        this.name = name;
        this.teacherIds = teacherIds;
        this.studentIds = studentIds;
    }

    // Геттеры и сеттеры
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

    public List<Long> getTeacherIds() {
        return teacherIds;
    }

    public void setTeacherIds(List<Long> teacherIds) {
        this.teacherIds = teacherIds;
    }

    public List<Long> getStudentIds() {
        return studentIds;
    }

    public void setStudentIds(List<Long> studentIds) {
        this.studentIds = studentIds;
    }
}