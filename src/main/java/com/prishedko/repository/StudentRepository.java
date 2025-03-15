package com.prishedko.repository;

import com.prishedko.entity.Course;
import com.prishedko.entity.School;
import com.prishedko.entity.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentRepository {
    private final Connection connection;

    public StudentRepository(Connection connection) {
        this.connection = connection;
    }

    /**
     * Сохраняет нового студента в базе данных
     */
    public Student save(Student student) throws SQLException {
        String sql = "INSERT INTO students (name, school_id) VALUES (?, ?) RETURNING id";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, student.getName());
            ps.setLong(2, student.getSchool().getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                student.setId(rs.getLong("id"));
            }
            return student;
        }
    }

    /**
     * Ищет студента по Id
     */
    public Student findById(Long id) throws SQLException {
        // Запрос для получения студента
        String studentSql = "SELECT id, name, school_id FROM students WHERE id = ?";
        Student student = null;

        try (PreparedStatement ps = connection.prepareStatement(studentSql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                student = new Student();
                student.setId(rs.getLong("id"));
                student.setName(rs.getString("name"));

                School school = new School(rs.getLong("school_id"), null);
                student.setSchool(school);
                student.setCourses(new ArrayList<>());
            }
        }

        if (student == null) {
            return null;
        }

        // Запрос для получения курсов студента
        String courseSql = "SELECT c.id, c.name " +
                "FROM courses c " +
                "JOIN students_courses sc ON c.id = sc.course_id " +
                "WHERE sc.student_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(courseSql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Course course = new Course(
                        rs.getLong("id"),
                        rs.getString("name"),
                        new ArrayList<>(),
                        new ArrayList<>()
                );
                student.getCourses().add(course);
            }
        }

        return student;
    }

    /**
     * Обновляет данные студента
     */
    public Student update(Student student) throws SQLException {
        String sql = "UPDATE students SET name = ?, school_id = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, student.getName());
            ps.setLong(2, student.getSchool().getId());
            ps.setLong(3, student.getId());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new IllegalArgumentException("Student with id " + student.getId() + " not found");
            }
            return student;
        }
    }

    /**
     * Удаляет студента по ID
     */
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM students WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new IllegalArgumentException("Student with id " + id + " not found");
            }
        }
    }

    /**
     * Находит всех студентов в школе
     */
    public List<Student> findBySchoolId(Long schoolId) throws SQLException {
        String sql = "SELECT id, name, school_id FROM students WHERE school_id = ?";
        List<Student> students = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, schoolId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Student student = new Student();
                student.setId(rs.getLong("id"));
                student.setName(rs.getString("name"));
                student.setSchool(new School(schoolId));
                students.add(student);
            }
        }
        return students;
    }
}