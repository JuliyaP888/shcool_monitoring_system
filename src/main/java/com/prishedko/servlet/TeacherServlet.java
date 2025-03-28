package com.prishedko.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prishedko.dto.TeacherDTO;
import com.prishedko.repository.TeacherRepository;
import com.prishedko.service.TeacherService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class TeacherServlet extends HttpServlet {
    private TeacherService teacherService;
    private ObjectMapper objectMapper;

    @Override
    public void init() {
        teacherService = new TeacherService(new TeacherRepository());
        objectMapper = new ObjectMapper();
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
                    List<TeacherDTO> teachers = teacherService.getTeachersBySchool(schoolId);
                    objectMapper.writeValue(resp.getWriter(), teachers);
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
                TeacherDTO teacher = teacherService.getTeacher(id);
                objectMapper.writeValue(resp.getWriter(), teacher);
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
            TeacherDTO dto = objectMapper.readValue(req.getReader(), TeacherDTO.class);
            TeacherDTO created = teacherService.createTeacher(dto);
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
            TeacherDTO dto = objectMapper.readValue(req.getReader(), TeacherDTO.class);
            dto.setId(id);
            TeacherDTO updated = teacherService.updateTeacher(dto);
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
            teacherService.deleteTeacher(id);
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