package com.prishedko.repository;

import com.prishedko.config.DatabaseConfig;
import com.prishedko.entity.Course;
import com.prishedko.entity.School;
import com.prishedko.entity.Student;
import com.prishedko.entity.Teacher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CourseRepository {

    /**
     * Сохраняет новый курс и связывает его с учителями и студентами
     */
    public Course save(Course course) throws SQLException {
        Connection connection = DatabaseConfig.getDataSource().getConnection();

        connection.setAutoCommit(false); // Начинаем транзакцию
        try {
            // 1. Сохраняем курс в таблице courses
            String courseSql = "INSERT INTO courses (name) VALUES (?) RETURNING id";
            try (PreparedStatement ps = connection.prepareStatement(courseSql)) {
                ps.setString(1, course.getName());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    course.setId(rs.getLong("id"));
                }
            }

            // 2. Связываем курс с учителями в таблице teachers_courses
            if (course.getTeachers() != null && !course.getTeachers().isEmpty()) {
                String teacherCourseSql = "INSERT INTO teachers_courses (teacher_id, course_id) VALUES (?, ?)";
                try (PreparedStatement ps = connection.prepareStatement(teacherCourseSql)) {
                    for (Teacher teacher : course.getTeachers()) {
                        if (teacher.getId() != null) { // Проверяем, что учитель уже существует
                            ps.setLong(1, teacher.getId());
                            ps.setLong(2, course.getId());
                            ps.addBatch();
                        }
                    }
                    ps.executeBatch();
                }
            }

            // 3. Связываем курс со студентами в таблице students_courses
            if (course.getStudents() != null && !course.getStudents().isEmpty()) {
                String studentCourseSql = "INSERT INTO students_courses (student_id, course_id) VALUES (?, ?)";
                try (PreparedStatement ps = connection.prepareStatement(studentCourseSql)) {
                    for (Student student : course.getStudents()) {
                        if (student.getId() != null) { // Проверяем, что студент уже существует
                            ps.setLong(1, student.getId());
                            ps.setLong(2, course.getId());
                            ps.addBatch();
                        }
                    }
                    ps.executeBatch();
                }
            }

            connection.commit(); // Фиксируем транзакцию
            return course;
        } catch (SQLException e) {
            connection.rollback(); // Откатываем транзакцию в случае ошибки
            throw new SQLException("Failed to save course and its relations: " + e.getMessage(), e);
        } finally {
            connection.setAutoCommit(true); // Возвращаем режим автокоммита
        }
    }

    /**
     * Находит курс по ID вместе с его учителями и студентами
     */
    public Course findById(Long id) throws SQLException {
        try (Connection connection = DatabaseConfig.getDataSource().getConnection()) {

            String courseSql = "SELECT id, name FROM courses WHERE id = ?";
            Course course = null;

            try (PreparedStatement ps = connection.prepareStatement(courseSql)) {
                ps.setLong(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    course = new Course(
                            rs.getLong("id"),
                            rs.getString("name"),
                            new ArrayList<>(),
                            new ArrayList<>()
                    );
                }
            }

            if (course == null) {
                return null;
            }

            // Запрос для получения учителей курса
            String teacherSql = "SELECT t.id, t.name, t.school_id " +
                    "FROM teachers t " +
                    "JOIN teachers_courses tc ON t.id = tc.teacher_id " +
                    "WHERE tc.course_id = ?";
            try (PreparedStatement ps = connection.prepareStatement(teacherSql)) {
                ps.setLong(1, id);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Teacher teacher = new Teacher();
                    teacher.setId(rs.getLong("id"));
                    teacher.setName(rs.getString("name"));
                    School school = new School(rs.getLong("school_id"), null);
                    teacher.setSchool(school);
                    teacher.setCourses(new ArrayList<>());
                    course.getTeachers().add(teacher);
                }
            }

            // Запрос для получения студентов курса
            String studentSql = "SELECT s.id, s.name, s.school_id " +
                    "FROM students s " +
                    "JOIN students_courses sc ON s.id = sc.student_id " +
                    "WHERE sc.course_id = ?";
            try (PreparedStatement ps = connection.prepareStatement(studentSql)) {
                ps.setLong(1, id);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Student student = new Student();
                    student.setId(rs.getLong("id"));
                    student.setName(rs.getString("name"));
                    School school = new School(rs.getLong("school_id"), null);
                    student.setSchool(school);
                    student.setCourses(new ArrayList<>());
                    course.getStudents().add(student);
                }
            }

            return course;
        }
    }

    /**
     * Обновляет данные курса
     */
    public Course update(Course course) throws SQLException {
        try (Connection connection = DatabaseConfig.getDataSource().getConnection()) {

            String sql = "UPDATE courses SET name = ? WHERE id = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, course.getName());
                ps.setLong(2, course.getId());
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected == 0) {
                    throw new IllegalArgumentException("Course with id " + course.getId() + " not found");
                }
                return course;
            }
        }
    }

    /**
     * Удаляет курс по ID
     */
    public void delete(Long id) throws SQLException {
        try (Connection connection = DatabaseConfig.getDataSource().getConnection()) {

            String sql = "DELETE FROM courses WHERE id = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setLong(1, id);
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected == 0) {
                    throw new IllegalArgumentException("Course with id " + id + " not found");
                }
            }
        }
    }

    /**
     * Находит все курсы
     */
    public List<Course> findAll() throws SQLException {
        try (Connection connection = DatabaseConfig.getDataSource().getConnection()) {

            String sql = "SELECT id, name FROM courses";
            List<Course> courses = new ArrayList<>();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Course course = new Course(
                            rs.getLong("id"),
                            rs.getString("name"),
                            new ArrayList<>(),
                            new ArrayList<>()
                    );
                    courses.add(course);
                }
            }
            return courses;
        }
    }
}