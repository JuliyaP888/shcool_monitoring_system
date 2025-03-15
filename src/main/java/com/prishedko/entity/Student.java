package com.prishedko.entity;

import java.util.ArrayList;
import java.util.List;

public class Student {
    private Long id;
    private String name;
    private School school; // ManyToOne
    private List<Course> courses = new ArrayList<>(); // ManyToMany

    public Student() {
    }

    public Student(Long id, String name, School school, List<Course> courses) {
        this.id = id;
        this.name = name;
        this.school = school;
        this.courses = courses;
    }

    public Student(Long id) {
        this.id = id;
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

    public School getSchool() {
        return school;
    }

    public void setSchool(School school) {
        this.school = school;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }
}