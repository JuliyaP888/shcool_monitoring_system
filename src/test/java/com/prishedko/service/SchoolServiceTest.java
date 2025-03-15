package com.prishedko.service;

import com.prishedko.dto.SchoolDTO;
import com.prishedko.entity.School;
import com.prishedko.entity.Student;
import com.prishedko.entity.Teacher;
import com.prishedko.repository.SchoolRepository;
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
class SchoolServiceTest {

    @InjectMocks
    private SchoolService schoolService;

    @Mock
    private SchoolRepository schoolRepository;

    @BeforeEach
    void setUp() {
        // MockitoExtension автоматически инициализирует @InjectMocks и @Mock
    }

    // Тесты для createSchool
    @Test
    void createSchool_WithValidData_ReturnsCreatedSchoolDTO() throws SQLException {
        SchoolDTO inputDTO = new SchoolDTO(null, "Test School", null, null);
        School savedSchool = new School(1L, "Test School",
                Arrays.asList(new Teacher(3L), new Teacher(4L)),
                Arrays.asList(new Student(1L), new Student(2L)));

        when(schoolRepository.save(any(School.class))).thenReturn(savedSchool);

        SchoolDTO result = schoolService.createSchool(inputDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test School", result.getName());
        assertEquals(Arrays.asList(1L, 2L), result.getStudentIds());
        assertEquals(Arrays.asList(3L, 4L), result.getTeacherIds());
        verify(schoolRepository, times(1)).save(any(School.class));
    }

    @Test
    void createSchool_WithSQLException_ThrowsSQLException() throws SQLException {
        SchoolDTO inputDTO = new SchoolDTO(null, "Test School", null, null);

        when(schoolRepository.save(any(School.class))).thenThrow(new SQLException("DB error"));

        SQLException exception = assertThrows(SQLException.class, () -> schoolService.createSchool(inputDTO));
        assertEquals("DB error", exception.getMessage());
        verify(schoolRepository, times(1)).save(any(School.class));
    }

    // Тесты для getSchool
    @Test
    void getSchool_WithValidId_ReturnsSchoolDTO() throws SQLException {
        Long schoolId = 1L;
        School school = new School(schoolId, "Test School",
                Arrays.asList(new Teacher(3L), new Teacher(4L)),
                Arrays.asList(new Student(1L), new Student(2L)));

        when(schoolRepository.findById(schoolId)).thenReturn(school);

        SchoolDTO result = schoolService.getSchool(schoolId);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test School", result.getName());
        assertEquals(Arrays.asList(1L, 2L), result.getStudentIds());
        assertEquals(Arrays.asList(3L, 4L), result.getTeacherIds());
        verify(schoolRepository, times(1)).findById(schoolId);
    }

    @Test
    void getSchool_WithNonExistentId_ThrowsIllegalArgumentException() throws SQLException {
        Long schoolId = 1L;

        when(schoolRepository.findById(schoolId)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> schoolService.getSchool(schoolId));
        assertEquals("School not found", exception.getMessage());
        verify(schoolRepository, times(1)).findById(schoolId);
    }

    @Test
    void getSchool_WithSQLException_ThrowsSQLException() throws SQLException {
        Long schoolId = 1L;

        when(schoolRepository.findById(schoolId)).thenThrow(new SQLException("DB error"));

        SQLException exception = assertThrows(SQLException.class, () -> schoolService.getSchool(schoolId));
        assertEquals("DB error", exception.getMessage());
        verify(schoolRepository, times(1)).findById(schoolId);
    }

    // Тесты для deleteSchool
    @Test
    void deleteSchool_WithValidId_DeletesSchool() throws SQLException {
        Long schoolId = 1L;

        when(schoolRepository.existsById(schoolId)).thenReturn(true);

        schoolService.deleteSchool(schoolId);

        verify(schoolRepository, times(1)).existsById(schoolId);
        verify(schoolRepository, times(1)).delete(schoolId);
    }

    @Test
    void deleteSchool_WithNonExistentId_ThrowsIllegalArgumentException() throws SQLException {
        Long schoolId = 1L;

        when(schoolRepository.existsById(schoolId)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> schoolService.deleteSchool(schoolId));
        assertEquals("School with id 1 not found", exception.getMessage());
        verify(schoolRepository, times(1)).existsById(schoolId);
        verify(schoolRepository, never()).delete(schoolId);
    }

    @Test
    void deleteSchool_WithSQLException_ThrowsSQLException() throws SQLException {
        Long schoolId = 1L;

        when(schoolRepository.existsById(schoolId)).thenReturn(true);
        doThrow(new SQLException("DB error")).when(schoolRepository).delete(schoolId);

        SQLException exception = assertThrows(SQLException.class, () -> schoolService.deleteSchool(schoolId));
        assertEquals("DB error", exception.getMessage());
        verify(schoolRepository, times(1)).existsById(schoolId);
        verify(schoolRepository, times(1)).delete(schoolId);
    }

    // Тесты для updateSchool
    @Test
    void updateSchool_WithValidData_ReturnsUpdatedSchoolDTO() throws SQLException {
        SchoolDTO inputDTO = new SchoolDTO(1L, "Updated School", null, null);
        School updatedSchool = new School(1L, "Updated School",
                Arrays.asList(new Teacher(3L), new Teacher(4L)),
                Arrays.asList(new Student(1L), new Student(2L)));

        when(schoolRepository.update(any(School.class))).thenReturn(updatedSchool);

        SchoolDTO result = schoolService.updateSchool(inputDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Updated School", result.getName());
        assertEquals(Arrays.asList(1L, 2L), result.getStudentIds());
        assertEquals(Arrays.asList(3L, 4L), result.getTeacherIds());
        verify(schoolRepository, times(1)).update(any(School.class));
    }

    @Test
    void updateSchool_WithSQLException_ThrowsSQLException() throws SQLException {
        SchoolDTO inputDTO = new SchoolDTO(1L, "Updated School", null, null);

        when(schoolRepository.update(any(School.class))).thenThrow(new SQLException("DB error"));

        SQLException exception = assertThrows(SQLException.class, () -> schoolService.updateSchool(inputDTO));
        assertEquals("DB error", exception.getMessage());
        verify(schoolRepository, times(1)).update(any(School.class));
    }
}