package com.prishedko.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prishedko.dto.TeacherDTO;
import com.prishedko.service.TeacherService;
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
class TeacherServletTest {

    @InjectMocks
    private TeacherServlet teacherServlet;

    @Mock
    private TeacherService teacherService;

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
        teacherServlet = new TeacherServlet();
        try {
            java.lang.reflect.Field serviceField = TeacherServlet.class.getDeclaredField("teacherService");
            java.lang.reflect.Field mapperField = TeacherServlet.class.getDeclaredField("objectMapper");
            serviceField.setAccessible(true);
            mapperField.setAccessible(true);
            serviceField.set(teacherServlet, teacherService);
            mapperField.set(teacherServlet, objectMapper);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set fields in test setup", e);
        }
    }

    // Тесты для doGet
    @Test
    void doGet_RootPathWithoutSchoolId_ReturnsBadRequest() throws IOException {
        when(request.getPathInfo()).thenReturn("/");
        when(request.getParameter("schoolId")).thenReturn(null);

        teacherServlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "schoolId parameter is required for list");
    }

    @Test
    void doGet_RootPathWithInvalidSchoolId_ReturnsBadRequest() throws IOException {
        when(request.getPathInfo()).thenReturn("/");
        when(request.getParameter("schoolId")).thenReturn("invalid");

        teacherServlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
    }

    @Test
    void doGet_RootPathWithSchoolId_ReturnsTeachers() throws IOException, SQLException {
        Long schoolId = 1L;
        List<TeacherDTO> teachers = Arrays.asList(
                new TeacherDTO(1L, "Teacher1", schoolId, List.of()),
                new TeacherDTO(2L, "Teacher2", schoolId, List.of())
        );

        when(request.getPathInfo()).thenReturn("/");
        when(request.getParameter("schoolId")).thenReturn(schoolId.toString());
        when(teacherService.getTeachersBySchool(schoolId)).thenReturn(teachers);
        when(response.getWriter()).thenReturn(printWriter);

        teacherServlet.doGet(request, response);

        verify(response).setContentType("application/json");
        verify(objectMapper).writeValue(printWriter, teachers);
    }

    @Test
    void doGet_WithInvalidPath_ReturnsBadRequest() throws IOException {
        when(request.getPathInfo()).thenReturn("/invalid/path");

        teacherServlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
    }

    @Test
    void doGet_WithInvalidIdFormat_ReturnsBadRequest() throws IOException {
        when(request.getPathInfo()).thenReturn("/abc");

        teacherServlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
    }

    @Test
    void doGet_WithValidId_ReturnsTeacher() throws IOException, SQLException {
        Long teacherId = 1L;
        TeacherDTO teacherDTO = new TeacherDTO(teacherId, "Test Teacher", 1L, List.of());

        when(request.getPathInfo()).thenReturn("/" + teacherId);
        when(teacherService.getTeacher(teacherId)).thenReturn(teacherDTO);
        when(response.getWriter()).thenReturn(printWriter);

        teacherServlet.doGet(request, response);

        verify(response).setContentType("application/json");
        verify(objectMapper).writeValue(printWriter, teacherDTO);
    }

    @Test
    void doGet_WithNonExistentId_ReturnsNotFound() throws IOException, SQLException {
        Long teacherId = 1L;
        when(request.getPathInfo()).thenReturn("/" + teacherId);
        when(teacherService.getTeacher(teacherId)).thenThrow(new IllegalArgumentException("Teacher not found"));

        teacherServlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND, "Teacher not found");
    }

    @Test
    void doGet_WithSQLException_ReturnsInternalServerError() throws IOException, SQLException {
        Long teacherId = 1L;
        when(request.getPathInfo()).thenReturn("/" + teacherId);
        when(teacherService.getTeacher(teacherId)).thenThrow(new SQLException("DB error"));

        teacherServlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
    }

    // Тесты для doPost
    @Test
    void doPost_WithInvalidPath_ReturnsBadRequest() throws IOException {
        when(request.getPathInfo()).thenReturn("/invalid");

        teacherServlet.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path for POST");
    }

    @Test
    void doPost_WithValidData_CreatesTeacher() throws IOException, SQLException {
        TeacherDTO inputDTO = new TeacherDTO(null, "New Teacher", 1L, List.of());
        TeacherDTO createdDTO = new TeacherDTO(1L, "New Teacher", 1L, List.of());

        when(request.getPathInfo()).thenReturn("/");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("{\"name\":\"New Teacher\",\"schoolId\":1}")));
        when(objectMapper.readValue(any(Reader.class), eq(TeacherDTO.class))).thenReturn(inputDTO);
        when(teacherService.createTeacher(inputDTO)).thenReturn(createdDTO);
        when(response.getWriter()).thenReturn(printWriter);

        teacherServlet.doPost(request, response);

        verify(response).setContentType("application/json");
        verify(response).setStatus(HttpServletResponse.SC_CREATED);
        verify(objectMapper).writeValue(printWriter, createdDTO);
    }

    @Test
    void doPost_WithSQLException_ReturnsInternalServerError() throws IOException, SQLException {
        TeacherDTO inputDTO = new TeacherDTO(null, "New Teacher", 1L, List.of());

        when(request.getPathInfo()).thenReturn("/");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("{\"name\":\"New Teacher\",\"schoolId\":1}")));
        when(objectMapper.readValue(any(Reader.class), eq(TeacherDTO.class))).thenReturn(inputDTO);
        when(teacherService.createTeacher(inputDTO)).thenThrow(new SQLException("DB error"));

        teacherServlet.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
    }

    // Тесты для doPut
    @Test
    void doPut_WithNoPathInfo_ReturnsBadRequest() throws IOException {
        when(request.getPathInfo()).thenReturn("/");

        teacherServlet.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "ID required for update");
    }

    @Test
    void doPut_WithInvalidPath_ReturnsBadRequest() throws IOException {
        when(request.getPathInfo()).thenReturn("/invalid/path");

        teacherServlet.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
    }

    @Test
    void doPut_WithValidData_UpdatesTeacher() throws IOException, SQLException {
        Long teacherId = 1L;
        TeacherDTO inputDTO = new TeacherDTO(null, "Updated Teacher", 1L, List.of());
        TeacherDTO updatedDTO = new TeacherDTO(teacherId, "Updated Teacher", 1L, List.of());

        when(request.getPathInfo()).thenReturn("/" + teacherId);
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("{\"name\":\"Updated Teacher\",\"schoolId\":1}")));
        when(objectMapper.readValue(any(Reader.class), eq(TeacherDTO.class))).thenReturn(inputDTO);
        when(teacherService.updateTeacher(any(TeacherDTO.class))).thenReturn(updatedDTO);
        when(response.getWriter()).thenReturn(printWriter);

        teacherServlet.doPut(request, response);

        verify(response).setContentType("application/json");
        verify(objectMapper).writeValue(printWriter, updatedDTO);
    }

    @Test
    void doPut_WithNonExistentId_ReturnsNotFound() throws IOException, SQLException {
        Long teacherId = 1L;
        TeacherDTO inputDTO = new TeacherDTO(null, "Updated Teacher", 1L, List.of());

        when(request.getPathInfo()).thenReturn("/" + teacherId);
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("{\"name\":\"Updated Teacher\",\"schoolId\":1}")));
        when(objectMapper.readValue(any(Reader.class), eq(TeacherDTO.class))).thenReturn(inputDTO);
        when(teacherService.updateTeacher(any(TeacherDTO.class))).thenThrow(new IllegalArgumentException("Teacher not found"));

        teacherServlet.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND, "Teacher not found");
    }

    // Тесты для doDelete
    @Test
    void doDelete_WithNoPathInfo_ReturnsBadRequest() throws IOException {
        when(request.getPathInfo()).thenReturn("/");

        teacherServlet.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "ID required for delete");
    }

    @Test
    void doDelete_WithInvalidPath_ReturnsBadRequest() throws IOException {
        when(request.getPathInfo()).thenReturn("/invalid/path");

        teacherServlet.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
    }

    @Test
    void doDelete_WithValidId_DeletesTeacher() throws IOException, SQLException {
        Long teacherId = 1L;
        when(request.getPathInfo()).thenReturn("/" + teacherId);

        teacherServlet.doDelete(request, response);

        verify(teacherService).deleteTeacher(teacherId);
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    void doDelete_WithNonExistentId_ReturnsNotFound() throws IOException, SQLException {
        Long teacherId = 1L;
        when(request.getPathInfo()).thenReturn("/" + teacherId);
        doThrow(new IllegalArgumentException("Teacher not found")).when(teacherService).deleteTeacher(teacherId);

        teacherServlet.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND, "Teacher not found");
    }

    @Test
    void doDelete_WithSQLException_ReturnsInternalServerError() throws IOException, SQLException {
        Long teacherId = 1L;
        when(request.getPathInfo()).thenReturn("/" + teacherId);
        doThrow(new SQLException("DB error")).when(teacherService).deleteTeacher(teacherId);

        teacherServlet.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
    }
}