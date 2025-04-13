package com.prishedko.mapper;

import com.prishedko.dto.TeacherDTO;
import com.prishedko.entity.Teacher;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TeacherMapper {
    TeacherMapper INSTANCE = Mappers.getMapper(TeacherMapper.class);

    @Mapping(target = "schoolId", source = "school.id")
    @Mapping(target = "courseIds", expression = "java(teacher.getCourses().stream().map(com.prishedko.entity.Course::getId).toList())")
    TeacherDTO toDTO(Teacher teacher);

    @Mapping(target = "school", expression = "java(dto.getSchoolId() != null ? new com.prishedko.entity.School(dto.getSchoolId()) : null)")
    @Mapping(target = "courses", ignore = true)
    Teacher toEntity(TeacherDTO dto);
}