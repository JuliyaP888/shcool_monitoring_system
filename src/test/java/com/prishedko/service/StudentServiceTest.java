package com.prishedko.service;

import com.prishedko.dto.StudentDTO;
import com.prishedko.entity.Course;
import com.prishedko.entity.School;
import com.prishedko.entity.Student;
import com.prishedko.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @InjectMocks
    private StudentService studentService;

    @Mock
    private StudentRepository studentRepository;

    @BeforeEach
    void setUp() {
        // MockitoExtension автоматически инициализирует @InjectMocks и @Mock
    }

    // Тесты для createStudent
    @Test
    void createStudent_WithValidData_ReturnsCreatedStudentDTO() throws SQLException {
        StudentDTO inputDTO = new StudentDTO(null, "John Doe", 1L, null);
        Student savedStudent = new Student(1L, "John Doe", new School(1L), Arrays.asList(new Course(1L), new Course(2L)));

        when(studentRepository.save(any(Student.class))).thenReturn(savedStudent);

        StudentDTO result = studentService.createStudent(inputDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals(1L, result.getSchoolId());
        assertEquals(Arrays.asList(1L, 2L), result.getCourseIds());
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void createStudent_WithSQLException_ThrowsSQLException() throws SQLException {
        StudentDTO inputDTO = new StudentDTO(null, "John Doe", 1L, null);

        when(studentRepository.save(any(Student.class))).thenThrow(new SQLException("DB error"));

        SQLException exception = assertThrows(SQLException.class, () -> studentService.createStudent(inputDTO));
        assertEquals("DB error", exception.getMessage());
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    // Тесты для getStudent
    @Test
    void getStudent_WithValidId_ReturnsStudentDTO() throws SQLException {
        Long studentId = 1L;
        Student student = new Student(studentId, "Jane Doe", new School(1L), Arrays.asList(new Course(1L), new Course(2L)));

        when(studentRepository.findById(studentId)).thenReturn(student);

        StudentDTO result = studentService.getStudent(studentId);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Jane Doe", result.getName());
        assertEquals(1L, result.getSchoolId());
        assertEquals(Arrays.asList(1L, 2L), result.getCourseIds());
        verify(studentRepository, times(1)).findById(studentId);
    }

    @Test
    void getStudent_WithNonExistentId_ThrowsIllegalArgumentException() throws SQLException {
        Long studentId = 1L;

        when(studentRepository.findById(studentId)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> studentService.getStudent(studentId));
        assertEquals("Student with id 1 not found", exception.getMessage());
        verify(studentRepository, times(1)).findById(studentId);
    }

    @Test
    void getStudent_WithSQLException_ThrowsSQLException() throws SQLException {
        Long studentId = 1L;

        when(studentRepository.findById(studentId)).thenThrow(new SQLException("DB error"));

        SQLException exception = assertThrows(SQLException.class, () -> studentService.getStudent(studentId));
        assertEquals("DB error", exception.getMessage());
        verify(studentRepository, times(1)).findById(studentId);
    }

    // Тесты для updateStudent
    @Test
    void updateStudent_WithValidData_ReturnsUpdatedStudentDTO() throws SQLException {
        StudentDTO inputDTO = new StudentDTO(1L, "Updated Student", 1L, null);
        Student updatedStudent = new Student(1L, "Updated Student", new School(1L), Arrays.asList(new Course(1L), new Course(2L)));

        when(studentRepository.update(any(Student.class))).thenReturn(updatedStudent);

        StudentDTO result = studentService.updateStudent(inputDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Updated Student", result.getName());
        assertEquals(1L, result.getSchoolId());
        assertEquals(Arrays.asList(1L, 2L), result.getCourseIds());
        verify(studentRepository, times(1)).update(any(Student.class));
    }

    @Test
    void updateStudent_WithNullId_ThrowsIllegalArgumentException() throws SQLException {
        StudentDTO inputDTO = new StudentDTO(null, "Updated Student", 1L, null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> studentService.updateStudent(inputDTO));
        assertEquals("Student ID cannot be null for update", exception.getMessage());
        verify(studentRepository, never()).update(any(Student.class));
    }

    @Test
    void updateStudent_WithSQLException_ThrowsSQLException() throws SQLException {
        StudentDTO inputDTO = new StudentDTO(1L, "Updated Student", 1L, null);

        when(studentRepository.update(any(Student.class))).thenThrow(new SQLException("DB error"));

        SQLException exception = assertThrows(SQLException.class, () -> studentService.updateStudent(inputDTO));
        assertEquals("DB error", exception.getMessage());
        verify(studentRepository, times(1)).update(any(Student.class));
    }

    // Тесты для deleteStudent
    @Test
    void deleteStudent_WithValidId_DeletesStudent() throws SQLException {
        Long studentId = 1L;

        studentService.deleteStudent(studentId);

        verify(studentRepository, times(1)).delete(studentId);
    }

    @Test
    void deleteStudent_WithSQLException_ThrowsSQLException() throws SQLException {
        Long studentId = 1L;

        doThrow(new SQLException("DB error")).when(studentRepository).delete(studentId);

        SQLException exception = assertThrows(SQLException.class, () -> studentService.deleteStudent(studentId));
        assertEquals("DB error", exception.getMessage());
        verify(studentRepository, times(1)).delete(studentId);
    }

    // Тесты для getStudentsBySchool
    @Test
    void getStudentsBySchool_WithValidSchoolId_ReturnsListOfStudentDTOs() throws SQLException {
        Long schoolId = 1L;
        List<Student> students = Arrays.asList(
                new Student(1L, "Student1", new School(schoolId), List.of(new Course(1L))),
                new Student(2L, "Student2", new School(schoolId), List.of(new Course(2L)))
        );

        when(studentRepository.findBySchoolId(schoolId)).thenReturn(students);

        List<StudentDTO> result = studentService.getStudentsBySchool(schoolId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Student1", result.get(0).getName());
        assertEquals(schoolId, result.get(0).getSchoolId());
        assertEquals(List.of(1L), result.get(0).getCourseIds());
        assertEquals(2L, result.get(1).getId());
        assertEquals("Student2", result.get(1).getName());
        assertEquals(schoolId, result.get(1).getSchoolId());
        assertEquals(List.of(2L), result.get(1).getCourseIds());
        verify(studentRepository, times(1)).findBySchoolId(schoolId);
    }

    @Test
    void getStudentsBySchool_WithSQLException_ThrowsSQLException() throws SQLException {
        Long schoolId = 1L;

        when(studentRepository.findBySchoolId(schoolId)).thenThrow(new SQLException("DB error"));

        SQLException exception = assertThrows(SQLException.class, () -> studentService.getStudentsBySchool(schoolId));
        assertEquals("DB error", exception.getMessage());
        verify(studentRepository, times(1)).findBySchoolId(schoolId);
    }
}