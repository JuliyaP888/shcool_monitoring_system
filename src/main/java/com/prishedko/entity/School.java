package com.prishedko.entity;

import java.util.ArrayList;
import java.util.List;

public class School {
    private Long id;
    private String name;
    private List<Teacher> teachers = new ArrayList<>(); // OneToMany
    private List<Student> students = new ArrayList<>(); // OneToMany

    public School() {
    }

    public School(long schoolId) {
        id = schoolId;
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

    public List<Teacher> getTeachers() {
        return teachers;
    }

    public void setTeachers(List<Teacher> teachers) {
        this.teachers = teachers;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }
}