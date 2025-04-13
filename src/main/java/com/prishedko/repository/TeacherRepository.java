package com.prishedko.repository;

import com.prishedko.entity.Course;
import com.prishedko.entity.School;
import com.prishedko.entity.Teacher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TeacherRepository {

    private final JdbcTemplate jdbcTemplate;

    public TeacherRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Teacher save(Teacher teacher) {
        String sql = "INSERT INTO teachers (name, school_id) VALUES (?, ?) RETURNING id";
        Long id = jdbcTemplate.queryForObject(sql, Long.class, teacher.getName(), teacher.getSchool().getId());
        teacher.setId(id);
        return teacher;
    }

    public Teacher findById(Long id) {
        String teacherSql = "SELECT id, name, school_id FROM teachers WHERE id = ?";
        List<Teacher> teachers = jdbcTemplate.query(teacherSql, new TeacherRowMapper(), id);
        if (teachers.isEmpty()) {
            return null;
        }
        Teacher teacher = teachers.get(0);

        // Загрузка курсов
        String courseSql = "SELECT c.id, c.name FROM courses c JOIN teachers_courses tc ON c.id = tc.course_id WHERE tc.teacher_id = ?";
        List<Course> courses = jdbcTemplate.query(courseSql, new CourseRowMapper(), id);
        teacher.setCourses(courses);

        return teacher;
    }

    public Teacher update(Teacher teacher) {
        String sql = "UPDATE teachers SET name = ?, school_id = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, teacher.getName(), teacher.getSchool().getId(), teacher.getId());
        if (rowsAffected == 0) {
            throw new IllegalArgumentException("Teacher with id " + teacher.getId() + " not found");
        }
        return teacher;
    }

    public void delete(Long id) {
        String sql = "DELETE FROM teachers WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        if (rowsAffected == 0) {
            throw new IllegalArgumentException("Teacher with id " + id + " not found");
        }
    }

    public List<Teacher> findBySchoolId(Long schoolId) {
        String teacherSql = "SELECT id, name, school_id FROM teachers WHERE school_id = ?";
        List<Teacher> teachers = jdbcTemplate.query(teacherSql, new TeacherRowMapper(), schoolId);
        if (teachers.isEmpty()) {
            return teachers;
        }

        // Загрузка курсов для всех учителей
        String courseSql = "SELECT tc.teacher_id, c.id, c.name FROM courses c " +
                "JOIN teachers_courses tc ON c.id = tc.course_id " +
                "WHERE tc.teacher_id IN (" +
                String.join(",", teachers.stream().map(t -> "?").toList()) + ")";
        List<Long> params = teachers.stream().map(Teacher::getId).toList();
        List<Course> courses = jdbcTemplate.query(courseSql, params.toArray(), rs -> {
            List<Course> courseList = new ArrayList<>();
            while (rs.next()) {
                Course course = new Course();
                course.setId(rs.getLong("id"));
                course.setName(rs.getString("name"));
                course.setTeachers(new ArrayList<>());
                course.setStudents(new ArrayList<>());
                courseList.add(course);
                Long teacherId = rs.getLong("teacher_id");
                teachers.stream()
                        .filter(t -> t.getId().equals(teacherId))
                        .findFirst()
                        .ifPresent(t -> t.getCourses().add(course));
            }
            return courseList;
        });

        return teachers;
    }

    private static class TeacherRowMapper implements RowMapper<Teacher> {
        @Override
        public Teacher mapRow(ResultSet rs, int rowNum) throws SQLException {
            Teacher teacher = new Teacher();
            teacher.setId(rs.getLong("id"));
            teacher.setName(rs.getString("name"));
            School school = new School();
            school.setId(rs.getLong("school_id"));
            teacher.setSchool(school);
            teacher.setCourses(new ArrayList<>());
            return teacher;
        }
    }

    private static class CourseRowMapper implements RowMapper<Course> {
        @Override
        public Course mapRow(ResultSet rs, int rowNum) throws SQLException {
            Course course = new Course();
            course.setId(rs.getLong("id"));
            course.setName(rs.getString("name"));
            course.setTeachers(new ArrayList<>());
            course.setStudents(new ArrayList<>());
            return course;
        }
    }
}