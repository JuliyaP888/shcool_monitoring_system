package com.prishedko.repository;

import com.prishedko.entity.Course;
import com.prishedko.entity.School;
import com.prishedko.entity.Teacher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TeacherRepository {
    private final Connection connection;

    public TeacherRepository(Connection connection) {
        this.connection = connection;
    }

    /**
     * Сохраняет нового учителя в базе данных
     */
    public Teacher save(Teacher teacher) throws SQLException {
        String sql = "INSERT INTO teachers (name, school_id) VALUES (?, ?) RETURNING id";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, teacher.getName());
            ps.setLong(2, teacher.getSchool().getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                teacher.setId(rs.getLong("id"));
            }
            return teacher;
        }
    }

    /**
     * Находит учителя по ID вместе с его школой и курсами
     */
    public Teacher findById(Long id) throws SQLException {
        // Запрос для получения учителя и школы
        String teacherSql = "SELECT id, name, school_id FROM teachers WHERE id = ?";
        Teacher teacher = null;

        try (PreparedStatement ps = connection.prepareStatement(teacherSql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                teacher = new Teacher();
                teacher.setId(rs.getLong("id"));
                teacher.setName(rs.getString("name"));

                School school = new School(rs.getLong("school_id"), null); // Имя школы можно загрузить отдельно
                teacher.setSchool(school);
                teacher.setCourses(new ArrayList<>()); // Инициализируем список курсов
            }
        }

        if (teacher == null) {
            return null;
        }

        // Запрос для получения курсов учителя
        String courseSql = "SELECT c.id, c.name " +
                "FROM courses c " +
                "JOIN teachers_courses tc ON c.id = tc.course_id " +
                "WHERE tc.teacher_id = ?";
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
                teacher.getCourses().add(course);
            }
        }

        return teacher;
    }

    /**
     * Обновляет данные учителя
     */
    public Teacher update(Teacher teacher) throws SQLException {
        String sql = "UPDATE teachers SET name = ?, school_id = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, teacher.getName());
            ps.setLong(2, teacher.getSchool().getId());
            ps.setLong(3, teacher.getId());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new IllegalArgumentException("Teacher with id " + teacher.getId() + " not found");
            }
            return teacher;
        }
    }

    /**
     * Удаляет учителя по ID
     */
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM teachers WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new IllegalArgumentException("Teacher with id " + id + " not found");
            }
        }
    }

    /**
     * Находит всех учителей в школе вместе с их курсами
     */
    public List<Teacher> findBySchoolId(Long schoolId) throws SQLException {
        // Запрос для получения учителей
        String teacherSql = "SELECT id, name, school_id FROM teachers WHERE school_id = ?";
        List<Teacher> teachers = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(teacherSql)) {
            ps.setLong(1, schoolId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Teacher teacher = new Teacher();
                teacher.setId(rs.getLong("id"));
                teacher.setName(rs.getString("name"));

                School school = new School(rs.getLong("school_id"), null); // Имя школы можно загрузить отдельно
                teacher.setSchool(school);
                teacher.setCourses(new ArrayList<>());

                teachers.add(teacher);
            }
        }

        // Если учителей нет, возвращаем пустой список
        if (teachers.isEmpty()) {
            return teachers;
        }

        // Запрос для получения курсов для всех найденных учителей
        String courseSql = "SELECT tc.teacher_id, c.id, c.name " +
                "FROM courses c " +
                "JOIN teachers_courses tc ON c.id = tc.course_id " +
                "WHERE tc.teacher_id IN (" +
                String.join(",", teachers.stream().map(t -> t.getId().toString()).toList()) +
                ")";
        try (PreparedStatement ps = connection.prepareStatement(courseSql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Long teacherId = rs.getLong("teacher_id");
                Course course = new Course(
                        rs.getLong("id"),
                        rs.getString("name"),
                        new ArrayList<>(),
                        new ArrayList<>()
                );
                // Находим учителя по ID и добавляем курс
                teachers.stream()
                        .filter(t -> t.getId().equals(teacherId))
                        .findFirst()
                        .ifPresent(t -> t.getCourses().add(course));
            }
        }

        return teachers;
    }
}