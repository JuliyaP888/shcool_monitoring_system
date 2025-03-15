package com.prishedko.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prishedko.config.DatabaseConfig;
import com.prishedko.dto.StudentDTO;
import com.prishedko.repository.StudentRepository;
import com.prishedko.service.StudentService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class StudentServlet extends HttpServlet {
    private StudentService studentService;
    private ObjectMapper objectMapper;

    @Override
    public void init() {
        try {
            studentService = new StudentService(
                    new StudentRepository(DatabaseConfig.getDataSource().getConnection())
            );
            objectMapper = new ObjectMapper();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize StudentServlet", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        resp.setContentType("application/json");

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                String schoolIdParam = req.getParameter("schoolId");
                if (schoolIdParam != null) {
                    Long schoolId = Long.parseLong(schoolIdParam);
                    List<StudentDTO> students = studentService.getStudentsBySchool(schoolId);
                    objectMapper.writeValue(resp.getWriter(), students);
                } else {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "schoolId parameter is required for list");
                }
            } else {
                String[] splits = pathInfo.split("/");
                if (splits.length != 2) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
                    return;
                }
                Long id = Long.parseLong(splits[1]);
                StudentDTO student = studentService.getStudent(id);
                objectMapper.writeValue(resp.getWriter(), student);
            }
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && !pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path for POST");
            return;
        }

        resp.setContentType("application/json");
        try {
            StudentDTO dto = objectMapper.readValue(req.getReader(), StudentDTO.class);
            StudentDTO created = studentService.createStudent(dto);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(resp.getWriter(), created);
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID required for update");
            return;
        }

        resp.setContentType("application/json");
        try {
            String[] splits = pathInfo.split("/");
            if (splits.length != 2) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
                return;
            }
            Long id = Long.parseLong(splits[1]);
            StudentDTO dto = objectMapper.readValue(req.getReader(), StudentDTO.class);
            dto.setId(id);
            StudentDTO updated = studentService.updateStudent(dto);
            objectMapper.writeValue(resp.getWriter(), updated);
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID required for delete");
            return;
        }

        try {
            String[] splits = pathInfo.split("/");
            if (splits.length != 2) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
                return;
            }
            Long id = Long.parseLong(splits[1]);
            studentService.deleteStudent(id);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }
}