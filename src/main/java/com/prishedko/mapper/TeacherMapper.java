package com.prishedko.mapper;

import com.prishedko.dto.TeacherDTO;
import com.prishedko.entity.Course;
import com.prishedko.entity.Teacher;

public class TeacherMapper {

    public static TeacherDTO mapToDTO(Teacher teacher) {
        TeacherDTO dto = new TeacherDTO();
        dto.setId(teacher.getId());
        dto.setName(teacher.getName());
        dto.setSchoolId(teacher.getSchool().getId());
        dto.setCourseIds(teacher.getCourses().stream().map(Course::getId).toList());
        return dto;
    }
}
