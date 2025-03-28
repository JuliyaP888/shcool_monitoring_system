package com.prishedko.mapper;

import com.prishedko.dto.CourseDTO;
import com.prishedko.entity.Course;
import com.prishedko.entity.Student;
import com.prishedko.entity.Teacher;

public class CourseMapper {

    public static CourseDTO mapToDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setStudentIds(course.getStudents().stream().map(Student::getId).toList());
        dto.setTeacherIds(course.getTeachers().stream().map(Teacher::getId).toList());
        return dto;
    }
}
