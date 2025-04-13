package com.prishedko.mapper;

import com.prishedko.dto.CourseDTO;
import com.prishedko.entity.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CourseMapper {
    CourseMapper INSTANCE = Mappers.getMapper(CourseMapper.class);

    @Mapping(target = "teacherIds", expression = "java(course.getTeachers().stream().map(com.prishedko.entity.Teacher::getId).toList())")
    @Mapping(target = "studentIds", expression = "java(course.getStudents().stream().map(com.prishedko.entity.Student::getId).toList())")
    CourseDTO toDTO(Course course);

    @Mapping(target = "teachers", expression = "java(dto.getTeacherIds() != null ? dto.getTeacherIds().stream().map(id ->{ com.prishedko.entity.Teacher t = new com.prishedko.entity.Teacher(); t.setId(id); return t; }).toList() : new java.util.ArrayList<>())")
    @Mapping(target = "students", expression = "java(dto.getStudentIds() != null ? dto.getStudentIds().stream().map(id -> { com.prishedko.entity.Student s = new com.prishedko.entity.Student(); s.setId(id); return s; }).toList() : new java.util.ArrayList<>())")
    Course toEntity(CourseDTO dto);
}