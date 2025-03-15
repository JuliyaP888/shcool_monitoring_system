package com.prishedko.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prishedko.dto.CourseDTO;
import com.prishedko.service.CourseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServletTest {

    @InjectMocks
    private CourseServlet courseServlet;

    @Mock
    private CourseService courseService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() {
        // Инициализация моков
        MockitoAnnotations.openMocks(this);

        // Устанавливаем зависимости вручную, чтобы избежать вызова init(), зависящего от БД
        courseServlet = new CourseServlet();
        try {
            java.lang.reflect.Field serviceField = CourseServlet.class.getDeclaredField("courseService");
            java.lang.reflect.Field mapperField = CourseServlet.class.getDeclaredField("objectMapper");
            serviceField.setAccessible(true);
            mapperField.setAccessible(true);
            serviceField.set(courseServlet, courseService);
            mapperField.set(courseServlet, objectMapper);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set fields in test setup", e);
        }
    }

    // Тесты для doGet
    @Test
    void doGet_RootPath_ReturnsAllCourses() throws IOException, SQLException {
        List<CourseDTO> courses = Arrays.asList(
                new CourseDTO(1L, "Course1", List.of(), List.of()),
                new CourseDTO(2L, "Course2", List.of(), List.of())
        );

        when(request.getPathInfo()).thenReturn("/");
        when(courseService.getAllCourses()).thenReturn(courses);
        when(response.getWriter()).thenReturn(printWriter);

        courseServlet.doGet(request, response);

        verify(response).setContentType("application/json");
        verify(objectMapper).writeValue(printWriter, courses);
    }

    @Test
    void doGet_WithInvalidPath_ReturnsBadRequest() throws IOException {
        when(request.getPathInfo()).thenReturn("/invalid/path");

        courseServlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
    }

    @Test
    void doGet_WithInvalidIdFormat_ReturnsBadRequest() throws IOException {
        when(request.getPathInfo()).thenReturn("/abc");

        courseServlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
    }

    @Test
    void doGet_WithValidId_ReturnsCourse() throws IOException, SQLException {
        Long courseId = 1L;
        CourseDTO courseDTO = new CourseDTO(courseId, "Test Course", List.of(), List.of());

        when(request.getPathInfo()).thenReturn("/" + courseId);
        when(courseService.getCourse(courseId)).thenReturn(courseDTO);
        when(response.getWriter()).thenReturn(printWriter);

        courseServlet.doGet(request, response);

        verify(response).setContentType("application/json");
        verify(objectMapper).writeValue(printWriter, courseDTO);
    }

    @Test
    void doGet_WithNonExistentId_ReturnsNotFound() throws IOException, SQLException {
        Long courseId = 1L;
        when(request.getPathInfo()).thenReturn("/" + courseId);
        when(courseService.getCourse(courseId)).thenThrow(new IllegalArgumentException("Course not found"));

        courseServlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND, "Course not found");
    }

    @Test
    void doGet_WithSQLException_ReturnsInternalServerError() throws IOException, SQLException {
        Long courseId = 1L;
        when(request.getPathInfo()).thenReturn("/" + courseId);
        when(courseService.getCourse(courseId)).thenThrow(new SQLException("DB error"));

        courseServlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
    }

    // Тесты для doPost
    @Test
    void doPost_WithInvalidPath_ReturnsBadRequest() throws IOException {
        when(request.getPathInfo()).thenReturn("/invalid");

        courseServlet.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path for POST");
    }

    @Test
    void doPost_WithValidData_CreatesCourse() throws IOException, SQLException {
        CourseDTO inputDTO = new CourseDTO(null, "New Course", List.of(1L), List.of(1L));
        CourseDTO createdDTO = new CourseDTO(1L, "New Course", List.of(1L), List.of(1L));

        when(request.getPathInfo()).thenReturn("/");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("{\"name\":\"New Course\",\"teacherIds\":[1],\"studentIds\":[1]}")));
        when(objectMapper.readValue(any(Reader.class), eq(CourseDTO.class))).thenReturn(inputDTO);
        when(courseService.createCourse(inputDTO)).thenReturn(createdDTO);
        when(response.getWriter()).thenReturn(printWriter);

        courseServlet.doPost(request, response);

        verify(response).setContentType("application/json");
        verify(response).setStatus(HttpServletResponse.SC_CREATED);
        verify(objectMapper).writeValue(printWriter, createdDTO);
    }

    @Test
    void doPost_WithSQLException_ReturnsInternalServerError() throws IOException, SQLException {
        CourseDTO inputDTO = new CourseDTO(null, "New Course", List.of(1L), List.of(1L));

        when(request.getPathInfo()).thenReturn("/");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("{\"name\":\"New Course\",\"teacherIds\":[1],\"studentIds\":[1]}")));
        when(objectMapper.readValue(any(Reader.class), eq(CourseDTO.class))).thenReturn(inputDTO);
        when(courseService.createCourse(inputDTO)).thenThrow(new SQLException("DB error"));

        courseServlet.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
    }

    // Тесты для doPut
    @Test
    void doPut_WithNoPathInfo_ReturnsBadRequest() throws IOException {
        when(request.getPathInfo()).thenReturn("/");

        courseServlet.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "ID required for update");
    }

    @Test
    void doPut_WithInvalidPath_ReturnsBadRequest() throws IOException {
        when(request.getPathInfo()).thenReturn("/invalid/path");

        courseServlet.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
    }

    @Test
    void doPut_WithValidData_UpdatesCourse() throws IOException, SQLException {
        Long courseId = 1L;
        CourseDTO inputDTO = new CourseDTO(null, "Updated Course", List.of(1L), List.of(1L));
        CourseDTO updatedDTO = new CourseDTO(courseId, "Updated Course", List.of(1L), List.of(1L));

        when(request.getPathInfo()).thenReturn("/" + courseId);
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("{\"name\":\"Updated Course\",\"teacherIds\":[1],\"studentIds\":[1]}")));
        when(objectMapper.readValue(any(Reader.class), eq(CourseDTO.class))).thenReturn(inputDTO);
        when(courseService.updateCourse(any(CourseDTO.class))).thenReturn(updatedDTO);
        when(response.getWriter()).thenReturn(printWriter);

        courseServlet.doPut(request, response);

        verify(response).setContentType("application/json");
        verify(objectMapper).writeValue(printWriter, updatedDTO);
    }

    @Test
    void doPut_WithNonExistentId_ReturnsNotFound() throws IOException, SQLException {
        Long courseId = 1L;
        CourseDTO inputDTO = new CourseDTO(null, "Updated Course", List.of(1L), List.of(1L));

        when(request.getPathInfo()).thenReturn("/" + courseId);
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("{\"name\":\"Updated Course\",\"teacherIds\":[1],\"studentIds\":[1]}")));
        when(objectMapper.readValue(any(Reader.class), eq(CourseDTO.class))).thenReturn(inputDTO);
        when(courseService.updateCourse(any(CourseDTO.class))).thenThrow(new IllegalArgumentException("Course not found"));

        courseServlet.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND, "Course not found");
    }

    // Тесты для doDelete
    @Test
    void doDelete_WithNoPathInfo_ReturnsBadRequest() throws IOException {
        when(request.getPathInfo()).thenReturn("/");

        courseServlet.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "ID required for delete");
    }

    @Test
    void doDelete_WithInvalidPath_ReturnsBadRequest() throws IOException {
        when(request.getPathInfo()).thenReturn("/invalid/path");

        courseServlet.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
    }

    @Test
    void doDelete_WithValidId_DeletesCourse() throws IOException, SQLException {
        Long courseId = 1L;
        when(request.getPathInfo()).thenReturn("/" + courseId);

        courseServlet.doDelete(request, response);

        verify(courseService).deleteCourse(courseId);
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    void doDelete_WithNonExistentId_ReturnsNotFound() throws IOException, SQLException {
        Long courseId = 1L;
        when(request.getPathInfo()).thenReturn("/" + courseId);
        doThrow(new IllegalArgumentException("Course not found")).when(courseService).deleteCourse(courseId);

        courseServlet.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND, "Course not found");
    }

    @Test
    void doDelete_WithSQLException_ReturnsInternalServerError() throws IOException, SQLException {
        Long courseId = 1L;
        when(request.getPathInfo()).thenReturn("/" + courseId);
        doThrow(new SQLException("DB error")).when(courseService).deleteCourse(courseId);

        courseServlet.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
    }
}