package com.prishedko.service;

import com.prishedko.dto.StudentDTO;
import com.prishedko.entity.Student;
import com.prishedko.mapper.StudentMapper;
import com.prishedko.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public StudentDTO createStudent(StudentDTO dto) {
        Student student = StudentMapper.INSTANCE.toEntity(dto);
        Student saved = studentRepository.save(student);
        return StudentMapper.INSTANCE.toDTO(saved);
    }

    public StudentDTO getStudent(Long id) {
        Student student = studentRepository.findById(id);
        if (student == null) {
            throw new IllegalArgumentException("Student with id " + id + " not found");
        }
        return StudentMapper.INSTANCE.toDTO(student);
    }

    public StudentDTO updateStudent(StudentDTO dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("Student ID cannot be null for update");
        }
        Student student = StudentMapper.INSTANCE.toEntity(dto);
        Student updated = studentRepository.update(student);
        return StudentMapper.INSTANCE.toDTO(updated);
    }

    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id);
        if (student == null) {
            throw new IllegalArgumentException("Student with id " + id + " not found");
        }
        studentRepository.delete(id);
    }

    public List<StudentDTO> getStudentsBySchool(Long schoolId) {
        List<Student> students = studentRepository.findBySchoolId(schoolId);
        return students.stream()
                .map(StudentMapper.INSTANCE::toDTO)
                .toList();
    }
}