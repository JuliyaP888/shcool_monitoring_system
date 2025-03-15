package com.prishedko.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prishedko.config.DatabaseConfig;
import com.prishedko.dto.SchoolDTO;
import com.prishedko.repository.SchoolRepository;
import com.prishedko.service.SchoolService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

public class SchoolServlet extends HttpServlet {
    private SchoolService schoolService;
    private ObjectMapper objectMapper;

    @Override
    public void init() {
        try {
            schoolService = new SchoolService(
                    new SchoolRepository(DatabaseConfig.getDataSource().getConnection())
            );
            objectMapper = new ObjectMapper();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize servlet", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "List not implemented");
                return;
            }

            String[] splits = pathInfo.split("/");
            if (splits.length != 2) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
                return;
            }

            Long id = Long.parseLong(splits[1]);
            SchoolDTO school = schoolService.getSchool(id);
            resp.setContentType("application/json");
            objectMapper.writeValue(resp.getWriter(), school);
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

        try {
            SchoolDTO dto = objectMapper.readValue(req.getReader(), SchoolDTO.class);
            SchoolDTO created = schoolService.createSchool(dto);
            resp.setContentType("application/json");
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

        try {
            String[] splits = pathInfo.split("/");
            if (splits.length != 2) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
                return;
            }

            Long id = Long.parseLong(splits[1]);
            SchoolDTO dto = objectMapper.readValue(req.getReader(), SchoolDTO.class);
            dto.setId(id);
            SchoolDTO updated = schoolService.updateSchool(dto);
            resp.setContentType("application/json");
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
            schoolService.deleteSchool(id);
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