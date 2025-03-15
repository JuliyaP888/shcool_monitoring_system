package com.prishedko.service;


import com.prishedko.dto.StudentDTO;
import com.prishedko.entity.Course;
import com.prishedko.entity.School;
import com.prishedko.entity.Student;
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
        return mapToDTO(saved);
    }

    public StudentDTO getStudent(Long id) throws SQLException {
        Student student = studentRepository.findById(id);
        if (student == null) {
            throw new IllegalArgumentException("Student with id " + id + " not found");
        }
        return mapToDTO(student);
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
        return mapToDTO(updated);
    }

    public void deleteStudent(Long id) throws SQLException {
        studentRepository.delete(id);
    }

    public List<StudentDTO> getStudentsBySchool(Long schoolId) throws SQLException {
        List<Student> students = studentRepository.findBySchoolId(schoolId);
        return students.stream().map(this::mapToDTO).toList();
    }

    private StudentDTO mapToDTO(Student student) {
        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId());
        dto.setName(student.getName());
        dto.setSchoolId(student.getSchool().getId());
        dto.setCourseIds(student.getCourses().stream().map(Course::getId).toList());
        return dto;
    }
}