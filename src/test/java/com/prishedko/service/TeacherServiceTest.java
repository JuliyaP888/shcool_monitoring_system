package com.prishedko.service;

import com.prishedko.dto.TeacherDTO;
import com.prishedko.entity.Course;
import com.prishedko.entity.School;
import com.prishedko.entity.Teacher;
import com.prishedko.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {

    @InjectMocks
    private TeacherService teacherService;

    @Mock
    private TeacherRepository teacherRepository;

    @BeforeEach
    void setUp() {
        // MockitoExtension автоматически инициализирует @InjectMocks и @Mock
    }

    // Тесты для createTeacher
    @Test
    void createTeacher_WithValidData_ReturnsCreatedTeacherDTO() throws SQLException {
        TeacherDTO inputDTO = new TeacherDTO(null, "John Smith", 1L, null);
        Teacher savedTeacher = new Teacher(1L, "John Smith", new School(1L), Arrays.asList(new Course(1L), new Course(2L)));

        when(teacherRepository.save(any(Teacher.class))).thenReturn(savedTeacher);

        TeacherDTO result = teacherService.createTeacher(inputDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Smith", result.getName());
        assertEquals(1L, result.getSchoolId());
        assertEquals(Arrays.asList(1L, 2L), result.getCourseIds());
        verify(teacherRepository, times(1)).save(any(Teacher.class));
    }

    @Test
    void createTeacher_WithSQLException_ThrowsSQLException() throws SQLException {
        TeacherDTO inputDTO = new TeacherDTO(null, "John Smith", 1L, null);

        when(teacherRepository.save(any(Teacher.class))).thenThrow(new SQLException("DB error"));

        SQLException exception = assertThrows(SQLException.class, () -> teacherService.createTeacher(inputDTO));
        assertEquals("DB error", exception.getMessage());
        verify(teacherRepository, times(1)).save(any(Teacher.class));
    }

    // Тесты для getTeacher
    @Test
    void getTeacher_WithValidId_ReturnsTeacherDTO() throws SQLException {
        Long teacherId = 1L;
        Teacher teacher = new Teacher(teacherId, "Jane Smith", new School(1L), Arrays.asList(new Course(1L), new Course(2L)));

        when(teacherRepository.findById(teacherId)).thenReturn(teacher);

        TeacherDTO result = teacherService.getTeacher(teacherId);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Jane Smith", result.getName());
        assertEquals(1L, result.getSchoolId());
        assertEquals(Arrays.asList(1L, 2L), result.getCourseIds());
        verify(teacherRepository, times(1)).findById(teacherId);
    }

    @Test
    void getTeacher_WithNonExistentId_ThrowsIllegalArgumentException() throws SQLException {
        Long teacherId = 1L;

        when(teacherRepository.findById(teacherId)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> teacherService.getTeacher(teacherId));
        assertEquals("Teacher with id 1 not found", exception.getMessage());
        verify(teacherRepository, times(1)).findById(teacherId);
    }

    @Test
    void getTeacher_WithSQLException_ThrowsSQLException() throws SQLException {
        Long teacherId = 1L;

        when(teacherRepository.findById(teacherId)).thenThrow(new SQLException("DB error"));

        SQLException exception = assertThrows(SQLException.class, () -> teacherService.getTeacher(teacherId));
        assertEquals("DB error", exception.getMessage());
        verify(teacherRepository, times(1)).findById(teacherId);
    }

    // Тесты для updateTeacher
    @Test
    void updateTeacher_WithValidData_ReturnsUpdatedTeacherDTO() throws SQLException {
        TeacherDTO inputDTO = new TeacherDTO(1L, "Updated Teacher", 1L, null);
        Teacher updatedTeacher = new Teacher(1L, "Updated Teacher", new School(1L), Arrays.asList(new Course(1L), new Course(2L)));

        when(teacherRepository.update(any(Teacher.class))).thenReturn(updatedTeacher);

        TeacherDTO result = teacherService.updateTeacher(inputDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Updated Teacher", result.getName());
        assertEquals(1L, result.getSchoolId());
        assertEquals(Arrays.asList(1L, 2L), result.getCourseIds());
        verify(teacherRepository, times(1)).update(any(Teacher.class));
    }

    @Test
    void updateTeacher_WithNullId_ThrowsIllegalArgumentException() throws SQLException {
        TeacherDTO inputDTO = new TeacherDTO(null, "Updated Teacher", 1L, null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> teacherService.updateTeacher(inputDTO));
        assertEquals("Teacher ID cannot be null for update", exception.getMessage());
        verify(teacherRepository, never()).update(any(Teacher.class));
    }

    @Test
    void updateTeacher_WithSQLException_ThrowsSQLException() throws SQLException {
        TeacherDTO inputDTO = new TeacherDTO(1L, "Updated Teacher", 1L, null);

        when(teacherRepository.update(any(Teacher.class))).thenThrow(new SQLException("DB error"));

        SQLException exception = assertThrows(SQLException.class, () -> teacherService.updateTeacher(inputDTO));
        assertEquals("DB error", exception.getMessage());
        verify(teacherRepository, times(1)).update(any(Teacher.class));
    }

    // Тесты для deleteTeacher
    @Test
    void deleteTeacher_WithValidId_DeletesTeacher() throws SQLException {
        Long teacherId = 1L;

        teacherService.deleteTeacher(teacherId);

        verify(teacherRepository, times(1)).delete(teacherId);
    }

    @Test
    void deleteTeacher_WithSQLException_ThrowsSQLException() throws SQLException {
        Long teacherId = 1L;

        doThrow(new SQLException("DB error")).when(teacherRepository).delete(teacherId);

        SQLException exception = assertThrows(SQLException.class, () -> teacherService.deleteTeacher(teacherId));
        assertEquals("DB error", exception.getMessage());
        verify(teacherRepository, times(1)).delete(teacherId);
    }

    @Test
    void getTeachersBySchool_WithSQLException_ThrowsSQLException() throws SQLException {
        Long schoolId = 1L;

        when(teacherRepository.findBySchoolId(schoolId)).thenThrow(new SQLException("DB error"));

        SQLException exception = assertThrows(SQLException.class, () -> teacherService.getTeachersBySchool(schoolId));
        assertEquals("DB error", exception.getMessage());
        verify(teacherRepository, times(1)).findBySchoolId(schoolId);
    }
}