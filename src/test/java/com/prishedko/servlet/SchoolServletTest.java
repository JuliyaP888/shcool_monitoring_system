package com.prishedko.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prishedko.dto.SchoolDTO;
import com.prishedko.service.SchoolService;
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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchoolServletTest {

    @InjectMocks
    private SchoolServlet schoolServlet;

    @Mock
    private SchoolService schoolService;

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
        schoolServlet = new SchoolServlet();
        try {
            java.lang.reflect.Field serviceField = SchoolServlet.class.getDeclaredField("schoolService");
            java.lang.reflect.Field mapperField = SchoolServlet.class.getDeclaredField("objectMapper");
            serviceField.setAccessible(true);
            mapperField.setAccessible(true);
            serviceField.set(schoolServlet, schoolService);
            mapperField.set(schoolServlet, objectMapper);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set fields in test setup", e);
        }
    }

    // Тесты для doGet
    @Test
    void doGet_RootPath_ReturnsNotImplemented() throws IOException {
        when(request.getPathInfo()).thenReturn("/");

        schoolServlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "List not implemented");
    }

    @Test
    void doGet_WithInvalidPath_ReturnsBadRequest() throws IOException {
        when(request.getPathInfo()).thenReturn("/invalid/path");

        schoolServlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
    }

    @Test
    void doGet_WithInvalidIdFormat_ReturnsBadRequest() throws IOException {
        when(request.getPathInfo()).thenReturn("/abc");

        schoolServlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
    }

    @Test
    void doGet_WithValidId_ReturnsSchool() throws IOException, SQLException {
        Long schoolId = 1L;
        SchoolDTO schoolDTO = new SchoolDTO();
        schoolDTO.setId(schoolId);
        schoolDTO.setName("Test School");

        when(request.getPathInfo()).thenReturn("/" + schoolId);
        when(schoolService.getSchool(schoolId)).thenReturn(schoolDTO);
        when(response.getWriter()).thenReturn(printWriter); // Настраиваем только здесь

        schoolServlet.doGet(request, response);

        verify(response).setContentType("application/json");
        verify(objectMapper).writeValue(printWriter, schoolDTO);
    }

    @Test
    void doGet_WithNonExistentId_ReturnsNotFound() throws IOException, SQLException {
        Long schoolId = 1L;
        when(request.getPathInfo()).thenReturn("/" + schoolId);
        when(schoolService.getSchool(schoolId)).thenThrow(new IllegalArgumentException("School not found"));

        schoolServlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND, "School not found");
    }

    @Test
    void doGet_WithSQLException_ReturnsInternalServerError() throws IOException, SQLException {
        Long schoolId = 1L;
        when(request.getPathInfo()).thenReturn("/" + schoolId);
        when(schoolService.getSchool(schoolId)).thenThrow(new SQLException("DB error"));

        schoolServlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
    }

    // Тесты для doPost
    @Test
    void doPost_WithInvalidPath_ReturnsBadRequest() throws IOException {
        when(request.getPathInfo()).thenReturn("/invalid");

        schoolServlet.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path for POST");
    }

    @Test
    void doPost_WithValidData_CreatesSchool() throws IOException, SQLException {
        SchoolDTO inputDTO = new SchoolDTO();
        inputDTO.setName("New School");
        SchoolDTO createdDTO = new SchoolDTO();
        createdDTO.setId(1L);
        createdDTO.setName("New School");

        when(request.getPathInfo()).thenReturn("/");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("{\"name\":\"New School\"}")));
        when(objectMapper.readValue(any(Reader.class), eq(SchoolDTO.class))).thenReturn(inputDTO);
        when(schoolService.createSchool(inputDTO)).thenReturn(createdDTO);
        when(response.getWriter()).thenReturn(printWriter); // Настраиваем только здесь

        schoolServlet.doPost(request, response);

        verify(response).setContentType("application/json");
        verify(response).setStatus(HttpServletResponse.SC_CREATED);
        verify(objectMapper).writeValue(printWriter, createdDTO);
    }

    @Test
    void doPost_WithSQLException_ReturnsInternalServerError() throws IOException, SQLException {
        SchoolDTO inputDTO = new SchoolDTO();
        inputDTO.setName("New School");

        when(request.getPathInfo()).thenReturn("/");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("{\"name\":\"New School\"}")));
        when(objectMapper.readValue(any(Reader.class), eq(SchoolDTO.class))).thenReturn(inputDTO);
        when(schoolService.createSchool(inputDTO)).thenThrow(new SQLException("DB error"));

        schoolServlet.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
    }

    // Тесты для doPut
    @Test
    void doPut_WithNoPathInfo_ReturnsBadRequest() throws IOException {
        when(request.getPathInfo()).thenReturn("/");

        schoolServlet.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "ID required for update");
    }

    @Test
    void doPut_WithInvalidPath_ReturnsBadRequest() throws IOException {
        when(request.getPathInfo()).thenReturn("/invalid/path");

        schoolServlet.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
    }

    @Test
    void doPut_WithValidData_UpdatesSchool() throws IOException, SQLException {
        Long schoolId = 1L;
        SchoolDTO inputDTO = new SchoolDTO();
        inputDTO.setName("Updated School");
        SchoolDTO updatedDTO = new SchoolDTO();
        updatedDTO.setId(schoolId);
        updatedDTO.setName("Updated School");

        when(request.getPathInfo()).thenReturn("/" + schoolId);
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("{\"name\":\"Updated School\"}")));
        when(objectMapper.readValue(any(Reader.class), eq(SchoolDTO.class))).thenReturn(inputDTO);
        when(schoolService.updateSchool(any(SchoolDTO.class))).thenReturn(updatedDTO);
        when(response.getWriter()).thenReturn(printWriter); // Настраиваем только здесь

        schoolServlet.doPut(request, response);

        verify(response).setContentType("application/json");
        verify(objectMapper).writeValue(printWriter, updatedDTO);
    }

    @Test
    void doPut_WithNonExistentId_ReturnsNotFound() throws IOException, SQLException {
        Long schoolId = 1L;
        SchoolDTO inputDTO = new SchoolDTO();
        inputDTO.setName("Updated School");

        when(request.getPathInfo()).thenReturn("/" + schoolId);
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("{\"name\":\"Updated School\"}")));
        when(objectMapper.readValue(any(Reader.class), eq(SchoolDTO.class))).thenReturn(inputDTO);
        when(schoolService.updateSchool(any(SchoolDTO.class))).thenThrow(new IllegalArgumentException("School not found"));

        schoolServlet.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND, "School not found");
    }

    // Тесты для doDelete
    @Test
    void doDelete_WithNoPathInfo_ReturnsBadRequest() throws IOException {
        when(request.getPathInfo()).thenReturn("/");

        schoolServlet.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "ID required for delete");
    }

    @Test
    void doDelete_WithInvalidPath_ReturnsBadRequest() throws IOException {
        when(request.getPathInfo()).thenReturn("/invalid/path");

        schoolServlet.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
    }

    @Test
    void doDelete_WithValidId_DeletesSchool() throws IOException, SQLException {
        Long schoolId = 1L;
        when(request.getPathInfo()).thenReturn("/" + schoolId);

        schoolServlet.doDelete(request, response);

        verify(schoolService).deleteSchool(schoolId);
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    void doDelete_WithNonExistentId_ReturnsNotFound() throws IOException, SQLException {
        Long schoolId = 1L;
        when(request.getPathInfo()).thenReturn("/" + schoolId);
        doThrow(new IllegalArgumentException("School not found")).when(schoolService).deleteSchool(schoolId);

        schoolServlet.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND, "School not found");
    }

    @Test
    void doDelete_WithSQLException_ReturnsInternalServerError() throws IOException, SQLException {
        Long schoolId = 1L;
        when(request.getPathInfo()).thenReturn("/" + schoolId);
        doThrow(new SQLException("DB error")).when(schoolService).deleteSchool(schoolId);

        schoolServlet.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
    }
}