package com.prishedko.mapper;

import com.prishedko.dto.StudentDTO;
import com.prishedko.entity.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface StudentMapper {
    StudentMapper INSTANCE = Mappers.getMapper(StudentMapper.class);

    @Mapping(target = "schoolId", source = "school.id")
    @Mapping(target = "courseIds", expression = "java(student.getCourses().stream().map(com.prishedko.entity.Course::getId).toList())")
    StudentDTO toDTO(Student student);

    @Mapping(target = "school", expression = "java(dto.getSchoolId() != null ? new com.prishedko.entity.School(dto.getSchoolId()) : null)")
    @Mapping(target = "courses", ignore = true)
    Student toEntity(StudentDTO dto);
}