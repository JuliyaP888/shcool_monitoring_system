package com.prishedko.service;


import com.prishedko.dto.StudentDTO;
import com.prishedko.entity.School;
import com.prishedko.entity.Student;
import com.prishedko.mapper.StudentMapper;
import com.prishedko.repository.StudentRepository;

import java.sql.SQLException;
import java.util.List;

public class StudentService {
    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public StudentDTO createStudent(StudentDTO dto) throws SQLException {
        Student student = new Student();
        student.setName(dto.getName());
        School school = new School();
        school.setId(dto.getSchoolId());
        student.setSchool(school);

        Student saved = studentRepository.save(student);
        return StudentMapper.mapToDTO(saved);
    }

    public StudentDTO getStudent(Long id) throws SQLException {
        Student student = studentRepository.findById(id);
        if (student == null) {
            throw new IllegalArgumentException("Student with id " + id + " not found");
        }
        return StudentMapper.mapToDTO(student);
    }

    public StudentDTO updateStudent(StudentDTO dto) throws SQLException {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("Student ID cannot be null for update");
        }
        Student student = new Student();
        student.setId(dto.getId());
        student.setName(dto.getName());
        School school = new School();
        school.setId(dto.getSchoolId());
        student.setSchool(school);

        Student updated = studentRepository.update(student);
        return StudentMapper.mapToDTO(updated);
    }

    public void deleteStudent(Long id) throws SQLException {
        studentRepository.delete(id);
    }

    public List<StudentDTO> getStudentsBySchool(Long schoolId) throws SQLException {
        List<Student> students = studentRepository.findBySchoolId(schoolId);
        return students.stream()
                .map(StudentMapper::mapToDTO)
                .toList();
    }
}