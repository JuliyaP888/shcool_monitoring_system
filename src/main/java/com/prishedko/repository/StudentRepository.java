package com.prishedko.repository;

import com.prishedko.entity.Course;
import com.prishedko.entity.School;
import com.prishedko.entity.Student;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class StudentRepository {

    private final JdbcTemplate jdbcTemplate;

    public StudentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Student save(Student student) {
        String sql = "INSERT INTO students (name, school_id) VALUES (?, ?) RETURNING id";
        Long id = jdbcTemplate.queryForObject(sql, Long.class, student.getName(), student.getSchool().getId());
        student.setId(id);
        return student;
    }

    public Student findById(Long id) {
        String studentSql = "SELECT id, name, school_id FROM students WHERE id = ?";
        List<Student> students = jdbcTemplate.query(studentSql, new StudentRowMapper(), id);
        if (students.isEmpty()) {
            return null;
        }
        Student student = students.get(0);

        // Загрузка курсов
        String courseSql = "SELECT c.id, c.name FROM courses c JOIN students_courses sc ON c.id = sc.course_id WHERE sc.student_id = ?";
        List<Course> courses = jdbcTemplate.query(courseSql, new CourseRowMapper(), id);
        student.setCourses(courses);

        return student;
    }

    public Student update(Student student) {
        String sql = "UPDATE students SET name = ?, school_id = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, student.getName(), student.getSchool().getId(), student.getId());
        if (rowsAffected == 0) {
            throw new IllegalArgumentException("Student with id " + student.getId() + " not found");
        }
        return student;
    }

    public void delete(Long id) {
        String sql = "DELETE FROM students WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        if (rowsAffected == 0) {
            throw new IllegalArgumentException("Student with id " + id + " not found");
        }
    }

    public List<Student> findBySchoolId(Long schoolId) {
        String studentSql = "SELECT id, name, school_id FROM students WHERE school_id = ?";
        List<Student> students = jdbcTemplate.query(studentSql, new StudentRowMapper(), schoolId);
        if (students.isEmpty()) {
            return students;
        }

        // Загрузка курсов для всех студентов
        String courseSql = "SELECT sc.student_id, c.id, c.name FROM courses c " +
                "JOIN students_courses sc ON c.id = sc.course_id " +
                "WHERE sc.student_id IN (" +
                String.join(",", students.stream().map(s -> "?").toList()) + ")";
        List<Long> params = students.stream().map(Student::getId).toList();
        List<Course> courses = jdbcTemplate.query(courseSql, params.toArray(), rs -> {
            List<Course> courseList = new ArrayList<>();
            while (rs.next()) {
                Course course = new Course();
                course.setId(rs.getLong("id"));
                course.setName(rs.getString("name"));
                course.setTeachers(new ArrayList<>());
                course.setStudents(new ArrayList<>());
                courseList.add(course);
                Long studentId = rs.getLong("student_id");
                students.stream()
                        .filter(s -> s.getId().equals(studentId))
                        .findFirst()
                        .ifPresent(s -> s.getCourses().add(course));
            }
            return courseList;
        });

        return students;
    }

    private static class StudentRowMapper implements RowMapper<Student> {
        @Override
        public Student mapRow(ResultSet rs, int rowNum) throws SQLException {
            Student student = new Student();
            student.setId(rs.getLong("id"));
            student.setName(rs.getString("name"));
            School school = new School();
            school.setId(rs.getLong("school_id"));
            student.setSchool(school);
            student.setCourses(new ArrayList<>());
            return student;
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