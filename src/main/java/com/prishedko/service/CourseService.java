package com.prishedko.service;

import com.prishedko.dto.CourseDTO;
import com.prishedko.entity.Course;
import com.prishedko.mapper.CourseMapper;
import com.prishedko.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public CourseDTO createCourse(CourseDTO dto) {
        Course course = CourseMapper.INSTANCE.toEntity(dto);
        Course saved = courseRepository.save(course);
        return CourseMapper.INSTANCE.toDTO(saved);
    }

    public CourseDTO getCourse(Long id) {
        Course course = courseRepository.findById(id);
        if (course == null) {
            throw new IllegalArgumentException("Course with id " + id + " not found");
        }
        return CourseMapper.INSTANCE.toDTO(course);
    }

    public CourseDTO updateCourse(CourseDTO dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("Course ID cannot be null for update");
        }
        Course course = CourseMapper.INSTANCE.toEntity(dto);
        Course updated = courseRepository.update(course);
        return CourseMapper.INSTANCE.toDTO(updated);
    }

    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id);
        if (course == null) {
            throw new IllegalArgumentException("Course with id " + id + " not found");
        }
        courseRepository.delete(id);
    }

    public List<CourseDTO> getAllCourses() {
        List<Course> courses = courseRepository.findAll();
        return courses.stream()
                .map(CourseMapper.INSTANCE::toDTO)
                .toList();
    }
}