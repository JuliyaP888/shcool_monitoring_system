package com.prishedko.mapper;

import com.prishedko.dto.SchoolDTO;
import com.prishedko.entity.School;
import com.prishedko.entity.Student;
import com.prishedko.entity.Teacher;

public class SchoolMapper {
    public static SchoolDTO mapToDTO(School school) {
        SchoolDTO dto = new SchoolDTO();
        dto.setId(school.getId());
        dto.setName(school.getName());
        dto.setStudentIds(school.getStudents().stream().map(Student::getId).toList());
        dto.setTeacherIds(school.getTeachers().stream().map(Teacher::getId).toList());
        return dto;
    }
}
