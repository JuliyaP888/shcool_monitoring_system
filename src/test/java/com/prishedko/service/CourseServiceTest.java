package com.prishedko.service;

import com.prishedko.dto.CourseDTO;
import com.prishedko.entity.Course;
import com.prishedko.entity.Student;
import com.prishedko.entity.Teacher;
import com.prishedko.repository.CourseRepository;
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
class CourseServiceTest {

    @InjectMocks
    private CourseService courseService;

    @Mock
    private CourseRepository courseRepository;

    @BeforeEach
    void setUp() {
        // MockitoExtension автоматически инициализирует @InjectMocks и @Mock
    }

    // Тесты для createCourse
    @Test
    void createCourse_WithValidData_ReturnsCreatedCourseDTO() throws SQLException {
        CourseDTO inputDTO = new CourseDTO(null, "Math", Arrays.asList(1L, 2L), Arrays.asList(3L, 4L));
        Course savedCourse = new Course(1L, "Math",
                Arrays.asList(new Teacher(1L), new Teacher(2L)),
                Arrays.asList(new Student(3L), new Student(4L))
        );

        when(courseRepository.save(any(Course.class))).thenReturn(savedCourse);

        CourseDTO result = courseService.createCourse(inputDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Math", result.getName());
        assertEquals(Arrays.asList(1L, 2L), result.getTeacherIds());
        assertEquals(Arrays.asList(3L, 4L), result.getStudentIds());
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    void createCourse_WithSQLException_ThrowsSQLException() throws SQLException {
        CourseDTO inputDTO = new CourseDTO(null, "Math", List.of(1L), List.of(1L));

        when(courseRepository.save(any(Course.class))).thenThrow(new SQLException("DB error"));

        SQLException exception = assertThrows(SQLException.class, () -> courseService.createCourse(inputDTO));
        assertEquals("DB error", exception.getMessage());
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    // Тесты для getCourse
    @Test
    void getCourse_WithValidId_ReturnsCourseDTO() throws SQLException {
        Long courseId = 1L;
        Course course = new Course(courseId, "Physics",
                Arrays.asList(new Teacher(1L), new Teacher(2L)),
                Arrays.asList(new Student(3L), new Student(4L))
        );

        when(courseRepository.findById(courseId)).thenReturn(course);

        CourseDTO result = courseService.getCourse(courseId);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Physics", result.getName());
        assertEquals(Arrays.asList(1L, 2L), result.getTeacherIds());
        assertEquals(Arrays.asList(3L, 4L), result.getStudentIds());
        verify(courseRepository, times(1)).findById(courseId);
    }

    @Test
    void getCourse_WithNonExistentId_ThrowsIllegalArgumentException() throws SQLException {
        Long courseId = 1L;

        when(courseRepository.findById(courseId)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> courseService.getCourse(courseId));
        assertEquals("Course with id 1 not found", exception.getMessage());
        verify(courseRepository, times(1)).findById(courseId);
    }

    @Test
    void getCourse_WithSQLException_ThrowsSQLException() throws SQLException {
        Long courseId = 1L;

        when(courseRepository.findById(courseId)).thenThrow(new SQLException("DB error"));

        SQLException exception = assertThrows(SQLException.class, () -> courseService.getCourse(courseId));
        assertEquals("DB error", exception.getMessage());
        verify(courseRepository, times(1)).findById(courseId);
    }

    // Тесты для updateCourse
    @Test
    void updateCourse_WithValidData_ReturnsUpdatedCourseDTO() throws SQLException {
        CourseDTO inputDTO = new CourseDTO(1L, "Updated Math", Arrays.asList(1L, 2L), Arrays.asList(3L, 4L));
        Course updatedCourse = new Course(1L, "Updated Math", List.of(), List.of());

        when(courseRepository.update(any(Course.class))).thenReturn(updatedCourse);

        CourseDTO result = courseService.updateCourse(inputDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Updated Math", result.getName());
        assertEquals(List.of(), result.getTeacherIds()); // Пустые списки, так как update не обновляет связи
        assertEquals(List.of(), result.getStudentIds());
        verify(courseRepository, times(1)).update(any(Course.class));
    }

    @Test
    void updateCourse_WithNullId_ThrowsIllegalArgumentException() throws SQLException {
        CourseDTO inputDTO = new CourseDTO(null, "Updated Math", List.of(1L), List.of(1L));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> courseService.updateCourse(inputDTO));
        assertEquals("Course ID cannot be null for update", exception.getMessage());
        verify(courseRepository, never()).update(any(Course.class));
    }

    @Test
    void updateCourse_WithSQLException_ThrowsSQLException() throws SQLException {
        CourseDTO inputDTO = new CourseDTO(1L, "Updated Math", List.of(1L), List.of(1L));

        when(courseRepository.update(any(Course.class))).thenThrow(new SQLException("DB error"));

        SQLException exception = assertThrows(SQLException.class, () -> courseService.updateCourse(inputDTO));
        assertEquals("DB error", exception.getMessage());
        verify(courseRepository, times(1)).update(any(Course.class));
    }

    // Тесты для deleteCourse
    @Test
    void deleteCourse_WithValidId_DeletesCourse() throws SQLException {
        Long courseId = 1L;

        courseService.deleteCourse(courseId);

        verify(courseRepository, times(1)).delete(courseId);
    }

    @Test
    void deleteCourse_WithSQLException_ThrowsSQLException() throws SQLException {
        Long courseId = 1L;

        doThrow(new SQLException("DB error")).when(courseRepository).delete(courseId);

        SQLException exception = assertThrows(SQLException.class, () -> courseService.deleteCourse(courseId));
        assertEquals("DB error", exception.getMessage());
        verify(courseRepository, times(1)).delete(courseId);
    }

    // Тесты для getAllCourses
    @Test
    void getAllCourses_ReturnsListOfCourseDTOs() throws SQLException {
        List<Course> courses = Arrays.asList(
                new Course(1L, "Math", List.of(new Teacher(1L)), List.of(new Student(1L))),
                new Course(2L, "Physics", List.of(new Teacher(2L)), List.of(new Student(2L)))
        );

        when(courseRepository.findAll()).thenReturn(courses);

        List<CourseDTO> result = courseService.getAllCourses();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Math", result.get(0).getName());
        assertEquals(List.of(1L), result.get(0).getTeacherIds());
        assertEquals(List.of(1L), result.get(0).getStudentIds());
        assertEquals(2L, result.get(1).getId());
        assertEquals("Physics", result.get(1).getName());
        assertEquals(List.of(2L), result.get(1).getTeacherIds());
        assertEquals(List.of(2L), result.get(1).getStudentIds());
        verify(courseRepository, times(1)).findAll();
    }

    @Test
    void getAllCourses_WithSQLException_ThrowsSQLException() throws SQLException {
        when(courseRepository.findAll()).thenThrow(new SQLException("DB error"));

        SQLException exception = assertThrows(SQLException.class, () -> courseService.getAllCourses());
        assertEquals("DB error", exception.getMessage());
        verify(courseRepository, times(1)).findAll();
    }
}