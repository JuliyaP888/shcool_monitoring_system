package com.prishedko.service;

import com.prishedko.dto.TeacherDTO;
import com.prishedko.entity.Teacher;
import com.prishedko.mapper.TeacherMapper;
import com.prishedko.repository.TeacherRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;

    public TeacherService(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    public TeacherDTO createTeacher(TeacherDTO dto) {
        Teacher teacher = TeacherMapper.INSTANCE.toEntity(dto);
        Teacher saved = teacherRepository.save(teacher);
        return TeacherMapper.INSTANCE.toDTO(saved);
    }

    public TeacherDTO getTeacher(Long id) {
        Teacher teacher = teacherRepository.findById(id);
        if (teacher == null) {
            throw new IllegalArgumentException("Teacher with id " + id + " not found");
        }
        return TeacherMapper.INSTANCE.toDTO(teacher);
    }

    public TeacherDTO updateTeacher(TeacherDTO dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("Teacher ID cannot be null for update");
        }
        Teacher teacher = TeacherMapper.INSTANCE.toEntity(dto);
        Teacher updated = teacherRepository.update(teacher);
        return TeacherMapper.INSTANCE.toDTO(updated);
    }

    public void deleteTeacher(Long id) {
        Teacher teacher = teacherRepository.findById(id);
        if (teacher == null) {
            throw new IllegalArgumentException("Teacher with id " + id + " not found");
        }
        teacherRepository.delete(id);
    }

    public List<TeacherDTO> getTeachersBySchool(Long schoolId) {
        List<Teacher> teachers = teacherRepository.findBySchoolId(schoolId);
        return teachers.stream()
                .map(TeacherMapper.INSTANCE::toDTO)
                .toList();
    }
}