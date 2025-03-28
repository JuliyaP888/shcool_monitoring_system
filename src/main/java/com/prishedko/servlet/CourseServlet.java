package com.prishedko.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prishedko.dto.CourseDTO;
import com.prishedko.repository.CourseRepository;
import com.prishedko.service.CourseService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class CourseServlet extends HttpServlet {
    private CourseService courseService;
    private ObjectMapper objectMapper;

    @Override
    public void init() {
        courseService = new CourseService(new CourseRepository());
        objectMapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        resp.setContentType("application/json");

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                List<CourseDTO> courses = courseService.getAllCourses();
                objectMapper.writeValue(resp.getWriter(), courses);
            } else {
                String[] splits = pathInfo.split("/");
                if (splits.length != 2) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
                    return;
                }
                Long id = Long.parseLong(splits[1]);
                CourseDTO course = courseService.getCourse(id);
                objectMapper.writeValue(resp.getWriter(), course);
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
            CourseDTO dto = objectMapper.readValue(req.getReader(), CourseDTO.class);
            CourseDTO created = courseService.createCourse(dto);
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
            CourseDTO dto = objectMapper.readValue(req.getReader(), CourseDTO.class);
            dto.setId(id);
            CourseDTO updated = courseService.updateCourse(dto);
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
            courseService.deleteCourse(id);
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