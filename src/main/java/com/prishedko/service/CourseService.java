package com.prishedko.service;

import com.prishedko.dto.CourseDTO;
import com.prishedko.entity.Course;
import com.prishedko.entity.Student;
import com.prishedko.entity.Teacher;
import com.prishedko.mapper.CourseMapper;
import com.prishedko.repository.CourseRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CourseService {
    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public CourseDTO createCourse(CourseDTO dto) throws SQLException {
        Course course = new Course(
                null,
                dto.getName(),
                dto.getTeacherIds().stream().map(id -> new Teacher(id)).toList(),
                dto.getStudentIds().stream().map(id -> new Student(id)).toList()
        );
        Course saved = courseRepository.save(course);
        return CourseMapper.mapToDTO(saved);
    }

    public CourseDTO getCourse(Long id) throws SQLException {
        Course course = courseRepository.findById(id);
        if (course == null) {
            throw new IllegalArgumentException("Course with id " + id + " not found");
        }
        return CourseMapper.mapToDTO(course);
    }

    public CourseDTO updateCourse(CourseDTO dto) throws SQLException {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("Course ID cannot be null for update");
        }
        Course course = new Course(
                dto.getId(),
                dto.getName(),
                new ArrayList<>(),
                new ArrayList<>()
        );
        Course updated = courseRepository.update(course);
        return CourseMapper.mapToDTO(updated);
    }

    public void deleteCourse(Long id) throws SQLException {
        courseRepository.delete(id);
    }

    public List<CourseDTO> getAllCourses() throws SQLException {
        List<Course> courses = courseRepository.findAll();
        return courses.stream()
                .map(CourseMapper::mapToDTO)
                .toList();
    }
}