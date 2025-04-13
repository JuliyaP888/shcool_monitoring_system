package com.prishedko.repository;

import com.prishedko.entity.School;
import com.prishedko.entity.Student;
import com.prishedko.entity.Teacher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SchoolRepository {

    private final JdbcTemplate jdbcTemplate;

    public SchoolRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public School save(School school) {
        String sql = "INSERT INTO schools (name) VALUES (?) RETURNING id";
        Long id = jdbcTemplate.queryForObject(sql, Long.class, school.getName());
        school.setId(id);
        return school;
    }

    public School findById(Long id) {
        String schoolSql = "SELECT id, name FROM schools WHERE id = ?";
        List<School> schools = jdbcTemplate.query(schoolSql, new SchoolRowMapper(), id);
        if (schools.isEmpty()) {
            return null;
        }
        School school = schools.get(0);

        // Загрузка учителей
        String teacherSql = "SELECT id, name, school_id FROM teachers WHERE school_id = ?";
        List<Teacher> teachers = jdbcTemplate.query(teacherSql, new TeacherRowMapper(), id);
        school.setTeachers(teachers);

        // Загрузка студентов
        String studentSql = "SELECT id, name, school_id FROM students WHERE school_id = ?";
        List<Student> students = jdbcTemplate.query(studentSql, new StudentRowMapper(), id);
        school.setStudents(students);

        return school;
    }

    public School update(School school) {
        String sql = "UPDATE schools SET name = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, school.getName(), school.getId());
        if (rowsAffected == 0) {
            throw new IllegalArgumentException("School with id " + school.getId() + " not found");
        }
        return school;
    }

    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM schools WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    public void delete(Long id) {
        String sql = "DELETE FROM schools WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        if (rowsAffected == 0) {
            throw new IllegalArgumentException("School with id " + id + " not found");
        }
    }

    private static class SchoolRowMapper implements RowMapper<School> {
        @Override
        public School mapRow(ResultSet rs, int rowNum) throws SQLException {
            School school = new School();
            school.setId(rs.getLong("id"));
            school.setName(rs.getString("name"));
            school.setTeachers(new ArrayList<>());
            school.setStudents(new ArrayList<>());
            return school;
        }
    }

    private static class TeacherRowMapper implements RowMapper<Teacher> {
        @Override
        public Teacher mapRow(ResultSet rs, int rowNum) throws SQLException {
            Teacher teacher = new Teacher();
            teacher.setId(rs.getLong("id"));
            teacher.setName(rs.getString("name"));
            teacher.setSchool(new School(rs.getLong("school_id")));
            return teacher;
        }
    }

    private static class StudentRowMapper implements RowMapper<Student> {
        @Override
        public Student mapRow(ResultSet rs, int rowNum) throws SQLException {
            Student student = new Student();
            student.setId(rs.getLong("id"));
            student.setName(rs.getString("name"));
            student.setSchool(new School(rs.getLong("school_id")));
            return student;
        }
    }
}