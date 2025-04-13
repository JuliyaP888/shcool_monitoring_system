package com.prishedko.mapper;

import com.prishedko.dto.SchoolDTO;
import com.prishedko.entity.School;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SchoolMapper {
    SchoolMapper INSTANCE = Mappers.getMapper(SchoolMapper.class);

    @Mapping(target = "teacherIds", expression = "java(school.getTeachers().stream().map(com.prishedko.entity.Teacher::getId).toList())")
    @Mapping(target = "studentIds", expression = "java(school.getStudents().stream().map(com.prishedko.entity.Student::getId).toList())")
    SchoolDTO toDTO(School school);

    @Mapping(target = "teachers", ignore = true)
    @Mapping(target = "students", ignore = true)
    School toEntity(SchoolDTO dto);
}