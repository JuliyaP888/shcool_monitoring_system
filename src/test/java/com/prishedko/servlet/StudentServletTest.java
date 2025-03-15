package com.prishedko.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prishedko.dto.StudentDTO;
import com.prishedko.service.StudentService;
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
class StudentServletTest {

    @InjectMocks
    private StudentServlet studentServlet;

    @Mock
    private StudentService studentService;

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
        studentServlet = new StudentServlet();
        try {
            java.lang.reflect.Field serviceField = StudentServlet.class.getDeclaredField("studentService");
            java.lang.reflect.Field mapperField = StudentServlet.class.getDeclaredField("objectMapper");
            serviceField.setAccessible(true);
            mapperField.setAccessible(true);
            serviceField.set(studentServlet, studentService);
            mapperField.set(studentServlet, objectMapper);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set fields in test setup", e);
        }
    }

    // Тесты для doGet
    @Test
    void doGet_RootPathWithoutSchoolId_ReturnsBadRequest() throws IOException {
        when(request.getPathInfo()).thenReturn("/");
        when(request.getParameter("schoolId")).thenReturn(null);

        studentServlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "schoolId parameter is required for list");
    }

    @Test
    void doGet_RootPathWithInvalidSchoolId_ReturnsBadRequest() throws IOException {
        when(request.getPathInfo()).thenReturn("/");
        when(request.getParameter("schoolId")).thenReturn("invalid");

        studentServlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
    }

    @Test
    void doGet_RootPathWithSchoolId_ReturnsStudents() throws IOException, SQLException {
        Long schoolId = 1L;
        List<StudentDTO> students = Arrays.asList(
                new StudentDTO(1L, "Student1", schoolId, List.of()),
                new StudentDTO(2L, "Student2", schoolId, List.of())
        );

        when(request.getPathInfo()).thenReturn("/");
        when(request.getParameter("schoolId")).thenReturn(schoolId.toString());
        when(studentService.getStudentsBySchool(schoolId)).thenReturn(students);
        when(response.getWriter()).thenReturn(printWriter);

        studentServlet.doGet(request, response);

        verify(response).setContentType("application/json");
        verify(objectMapper).writeValue(printWriter, students);
    }

    @Test
    void doGet_WithInvalidPath_ReturnsBadRequest() throws IOException {
        when(request.getPathInfo()).thenReturn("/invalid/path");

        studentServlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
    }

    @Test
    void doGet_WithInvalidIdFormat_ReturnsBadRequest() throws IOException {
        when(request.getPathInfo()).thenReturn("/abc");

        studentServlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
    }

    @Test
    void doGet_WithValidId_ReturnsStudent() throws IOException, SQLException {
        Long studentId = 1L;
        StudentDTO studentDTO = new StudentDTO(studentId, "Test Student", 1L, List.of());

        when(request.getPathInfo()).thenReturn("/" + studentId);
        when(studentService.getStudent(studentId)).thenReturn(studentDTO);
        when(response.getWriter()).thenReturn(printWriter);

        studentServlet.doGet(request, response);

        verify(response).setContentType("application/json");
        verify(objectMapper).writeValue(printWriter, studentDTO);
    }

    @Test
    void doGet_WithNonExistentId_ReturnsNotFound() throws IOException, SQLException {
        Long studentId = 1L;
        when(request.getPathInfo()).thenReturn("/" + studentId);
        when(studentService.getStudent(studentId)).thenThrow(new IllegalArgumentException("Student not found"));

        studentServlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND, "Student not found");
    }

    @Test
    void doGet_WithSQLException_ReturnsInternalServerError() throws IOException, SQLException {
        Long studentId = 1L;
        when(request.getPathInfo()).thenReturn("/" + studentId);
        when(studentService.getStudent(studentId)).thenThrow(new SQLException("DB error"));

        studentServlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
    }

    // Тесты для doPost
    @Test
    void doPost_WithInvalidPath_ReturnsBadRequest() throws IOException {
        when(request.getPathInfo()).thenReturn("/invalid");

        studentServlet.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path for POST");
    }

    @Test
    void doPost_WithValidData_CreatesStudent() throws IOException, SQLException {
        StudentDTO inputDTO = new StudentDTO(null, "New Student", 1L, List.of());
        StudentDTO createdDTO = new StudentDTO(1L, "New Student", 1L, List.of());

        when(request.getPathInfo()).thenReturn("/");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("{\"name\":\"New Student\",\"schoolId\":1}")));
        when(objectMapper.readValue(any(Reader.class), eq(StudentDTO.class))).thenReturn(inputDTO);
        when(studentService.createStudent(inputDTO)).thenReturn(createdDTO);
        when(response.getWriter()).thenReturn(printWriter);

        studentServlet.doPost(request, response);

        verify(response).setContentType("application/json");
        verify(response).setStatus(HttpServletResponse.SC_CREATED);
        verify(objectMapper).writeValue(printWriter, createdDTO);
    }

    @Test
    void doPost_WithSQLException_ReturnsInternalServerError() throws IOException, SQLException {
        StudentDTO inputDTO = new StudentDTO(null, "New Student", 1L, List.of());

        when(request.getPathInfo()).thenReturn("/");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("{\"name\":\"New Student\",\"schoolId\":1}")));
        when(objectMapper.readValue(any(Reader.class), eq(StudentDTO.class))).thenReturn(inputDTO);
        when(studentService.createStudent(inputDTO)).thenThrow(new SQLException("DB error"));

        studentServlet.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
    }

    // Тесты для doPut
    @Test
    void doPut_WithNoPathInfo_ReturnsBadRequest() throws IOException {
        when(request.getPathInfo()).thenReturn("/");

        studentServlet.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "ID required for update");
    }

    @Test
    void doPut_WithInvalidPath_ReturnsBadRequest() throws IOException {
        when(request.getPathInfo()).thenReturn("/invalid/path");

        studentServlet.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
    }

    @Test
    void doPut_WithValidData_UpdatesStudent() throws IOException, SQLException {
        Long studentId = 1L;
        StudentDTO inputDTO = new StudentDTO(null, "Updated Student", 1L, List.of());
        StudentDTO updatedDTO = new StudentDTO(studentId, "Updated Student", 1L, List.of());

        when(request.getPathInfo()).thenReturn("/" + studentId);
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("{\"name\":\"Updated Student\",\"schoolId\":1}")));
        when(objectMapper.readValue(any(Reader.class), eq(StudentDTO.class))).thenReturn(inputDTO);
        when(studentService.updateStudent(any(StudentDTO.class))).thenReturn(updatedDTO);
        when(response.getWriter()).thenReturn(printWriter);

        studentServlet.doPut(request, response);

        verify(response).setContentType("application/json");
        verify(objectMapper).writeValue(printWriter, updatedDTO);
    }

    @Test
    void doPut_WithNonExistentId_ReturnsNotFound() throws IOException, SQLException {
        Long studentId = 1L;
        StudentDTO inputDTO = new StudentDTO(null, "Updated Student", 1L, List.of());

        when(request.getPathInfo()).thenReturn("/" + studentId);
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("{\"name\":\"Updated Student\",\"schoolId\":1}")));
        when(objectMapper.readValue(any(Reader.class), eq(StudentDTO.class))).thenReturn(inputDTO);
        when(studentService.updateStudent(any(StudentDTO.class))).thenThrow(new IllegalArgumentException("Student not found"));

        studentServlet.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND, "Student not found");
    }

    // Тесты для doDelete
    @Test
    void doDelete_WithNoPathInfo_ReturnsBadRequest() throws IOException {
        when(request.getPathInfo()).thenReturn("/");

        studentServlet.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "ID required for delete");
    }

    @Test
    void doDelete_WithInvalidPath_ReturnsBadRequest() throws IOException {
        when(request.getPathInfo()).thenReturn("/invalid/path");

        studentServlet.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
    }

    @Test
    void doDelete_WithValidId_DeletesStudent() throws IOException, SQLException {
        Long studentId = 1L;
        when(request.getPathInfo()).thenReturn("/" + studentId);

        studentServlet.doDelete(request, response);

        verify(studentService).deleteStudent(studentId);
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    void doDelete_WithNonExistentId_ReturnsNotFound() throws IOException, SQLException {
        Long studentId = 1L;
        when(request.getPathInfo()).thenReturn("/" + studentId);
        doThrow(new IllegalArgumentException("Student not found")).when(studentService).deleteStudent(studentId);

        studentServlet.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND, "Student not found");
    }

    @Test
    void doDelete_WithSQLException_ReturnsInternalServerError() throws IOException, SQLException {
        Long studentId = 1L;
        when(request.getPathInfo()).thenReturn("/" + studentId);
        doThrow(new SQLException("DB error")).when(studentService).deleteStudent(studentId);

        studentServlet.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
    }
}