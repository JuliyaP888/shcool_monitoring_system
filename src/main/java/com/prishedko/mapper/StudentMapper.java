package com.prishedko.mapper;

import com.prishedko.dto.StudentDTO;
import com.prishedko.entity.Course;
import com.prishedko.entity.Student;

public class StudentMapper {
    public static StudentDTO mapToDTO(Student student) {
        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId());
        dto.setName(student.getName());
        dto.setSchoolId(student.getSchool().getId());
        dto.setCourseIds(student.getCourses().stream().map(Course::getId).toList());
        return dto;
    }
}
