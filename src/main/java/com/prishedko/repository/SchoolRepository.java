package com.prishedko.repository;

import com.prishedko.config.DatabaseConfig;
import com.prishedko.entity.School;
import com.prishedko.entity.Student;
import com.prishedko.entity.Teacher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class SchoolRepository {

    /**
     * Создает школу
     */
    public School save(School school) throws SQLException {

        String sql = "INSERT INTO schools (name) VALUES (?) RETURNING id";
        try (
                Connection connection = DatabaseConfig.getDataSource().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, school.getName());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                school.setId(rs.getLong("id"));
            }
            return school;
        }
    }

    /**
     * Находит школу по ID вместе со связанными учителями и студентами
     */
    public School findById(Long id) throws SQLException {
        // Запрос для получения школы
        String schoolSql = "SELECT id, name FROM schools WHERE id = ?";
        School school = null;

        try (
                Connection connection = DatabaseConfig.getDataSource().getConnection();
                PreparedStatement ps = connection.prepareStatement(schoolSql)
        ) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                school = new School(rs.getLong("id"), rs.getString("name"));
                school.setTeachers(new ArrayList<>());
                school.setStudents(new ArrayList<>());
            }
        }

        if (school == null) {
            return null;
        }

        // Запрос для получения учителей школы
        String teacherSql = "SELECT id, name, school_id FROM teachers WHERE school_id = ?";
        try (
                Connection connection = DatabaseConfig.getDataSource().getConnection();
                PreparedStatement ps = connection.prepareStatement(teacherSql)
        ) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Teacher teacher = new Teacher();
                teacher.setId(rs.getLong("id"));
                teacher.setName(rs.getString("name"));
                teacher.setSchool(school);
                school.getTeachers().add(teacher);
            }
        }

        // Запрос для получения студентов школы
        String studentSql = "SELECT id, name, school_id FROM students WHERE school_id = ?";
        try (
                Connection connection = DatabaseConfig.getDataSource().getConnection();
                PreparedStatement ps = connection.prepareStatement(studentSql)
        ) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Student student = new Student();
                student.setId(rs.getLong("id"));
                student.setName(rs.getString("name"));
                student.setSchool(school);
                school.getStudents().add(student);
            }
        }

        return school;
    }

    /**
     * Обновляет информацию о школе
     */
    public School update(School school) throws SQLException {
        if (school.getId() == null) {
            throw new IllegalArgumentException("School ID cannot be null for update");
        }

        String sql = "UPDATE schools SET name = ? WHERE id = ?";

        try (
                Connection connection = DatabaseConfig.getDataSource().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, school.getName());
            ps.setLong(2, school.getId());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                throw new IllegalArgumentException("School with id " + school.getId() + " not found");
            }

            return school;
        }
    }

    /**
     * Проверяет существование школы по идентификатору
     */
    public boolean existsById(Long id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM schools WHERE id = ?";

        try (
                Connection connection = DatabaseConfig.getDataSource().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setLong(1, id);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        }
    }

    /**
     * Удаляет школу по идентификатору
     */
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM schools WHERE id = ?";

        try (
                Connection connection = DatabaseConfig.getDataSource().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setLong(1, id);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                throw new IllegalArgumentException("School with id " + id + " not found");
            }
        }
    }
}