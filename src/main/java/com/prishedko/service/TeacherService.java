package com.prishedko.service;

import com.prishedko.dto.TeacherDTO;
import com.prishedko.entity.School;
import com.prishedko.entity.Teacher;
import com.prishedko.mapper.TeacherMapper;
import com.prishedko.repository.TeacherRepository;

import java.sql.SQLException;
import java.util.List;

public class TeacherService {
    private final TeacherRepository teacherRepository;

    public TeacherService(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    public TeacherDTO createTeacher(TeacherDTO dto) throws SQLException {
        Teacher teacher = new Teacher();
        teacher.setName(dto.getName());
        School school = new School();
        school.setId(dto.getSchoolId());
        teacher.setSchool(school);

        Teacher saved = teacherRepository.save(teacher);
        return TeacherMapper.mapToDTO(saved);
    }

    public TeacherDTO getTeacher(Long id) throws SQLException {
        Teacher teacher = teacherRepository.findById(id);
        if (teacher == null) {
            throw new IllegalArgumentException("Teacher with id " + id + " not found");
        }
        return TeacherMapper.mapToDTO(teacher);
    }

    public TeacherDTO updateTeacher(TeacherDTO dto) throws SQLException {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("Teacher ID cannot be null for update");
        }
        Teacher teacher = new Teacher();
        teacher.setId(dto.getId());
        teacher.setName(dto.getName());
        School school = new School();
        school.setId(dto.getSchoolId());
        teacher.setSchool(school);

        Teacher updated = teacherRepository.update(teacher);
        return TeacherMapper.mapToDTO(updated);
    }

    public void deleteTeacher(Long id) throws SQLException {
        teacherRepository.delete(id);
    }

    public List<TeacherDTO> getTeachersBySchool(Long schoolId) throws SQLException {
        List<Teacher> teachers = teacherRepository.findBySchoolId(schoolId);
        return teachers.stream()
                .map(TeacherMapper::mapToDTO)
                .toList();
    }
}