package com.prishedko.repository;

import com.prishedko.entity.Course;
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
public class CourseRepository {

    private final JdbcTemplate jdbcTemplate;

    public CourseRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Course save(Course course) {
        // Сохраняем курс
        String courseSql = "INSERT INTO courses (name) VALUES (?) RETURNING id";
        Long id = jdbcTemplate.queryForObject(courseSql, Long.class, course.getName());
        course.setId(id);

        // Связываем с учителями
        if (course.getTeachers() != null && !course.getTeachers().isEmpty()) {
            String teacherCourseSql = "INSERT INTO teachers_courses (teacher_id, course_id) VALUES (?, ?)";
            jdbcTemplate.batchUpdate(teacherCourseSql, course.getTeachers().stream()
                    .filter(t -> t.getId() != null)
                    .map(t -> new Object[]{t.getId(), id})
                    .toList());
        }

        // Связываем со студентами
        if (course.getStudents() != null && !course.getStudents().isEmpty()) {
            String studentCourseSql = "INSERT INTO students_courses (student_id, course_id) VALUES (?, ?)";
            jdbcTemplate.batchUpdate(studentCourseSql, course.getStudents().stream()
                    .filter(s -> s.getId() != null)
                    .map(s -> new Object[]{s.getId(), id})
                    .toList());
        }

        return course;
    }

    public Course findById(Long id) {
        String courseSql = "SELECT id, name FROM courses WHERE id = ?";
        List<Course> courses = jdbcTemplate.query(courseSql, new CourseRowMapper(), id);
        if (courses.isEmpty()) {
            return null;
        }
        Course course = courses.get(0);

        // Загрузка учителей
        String teacherSql = "SELECT t.id, t.name, t.school_id FROM teachers t JOIN teachers_courses tc ON t.id = tc.teacher_id WHERE tc.course_id = ?";
        List<Teacher> teachers = jdbcTemplate.query(teacherSql, new TeacherRowMapper(), id);
        course.setTeachers(teachers);

        // Загрузка студентов
        String studentSql = "SELECT s.id, s.name, s.school_id FROM students s JOIN students_courses sc ON s.id = sc.student_id WHERE sc.course_id = ?";
        List<Student> students = jdbcTemplate.query(studentSql, new StudentRowMapper(), id);
        course.setStudents(students);

        return course;
    }

    public Course update(Course course) {
        String sql = "UPDATE courses SET name = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, course.getName(), course.getId());
        if (rowsAffected == 0) {
            throw new IllegalArgumentException("Course with id " + course.getId() + " not found");
        }
        return course;
    }

    public void delete(Long id) {
        String sql = "DELETE FROM courses WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        if (rowsAffected == 0) {
            throw new IllegalArgumentException("Course with id " + id + " not found");
        }
    }

    public List<Course> findAll() {
        String courseSql = "SELECT id, name FROM courses";
        List<Course> courses = jdbcTemplate.query(courseSql, new CourseRowMapper());
        if (courses.isEmpty()) {
            return courses;
        }

        // Загрузка учителей для всех курсов
        String teacherSql = "SELECT tc.course_id, t.id, t.name, t.school_id FROM teachers t " +
                "JOIN teachers_courses tc ON t.id = tc.teacher_id " +
                "WHERE tc.course_id IN (" + String.join(",", courses.stream().map(c -> "?").toList()) + ")";
        List<Long> teacherParams = courses.stream().map(Course::getId).toList();
        jdbcTemplate.query(teacherSql, teacherParams.toArray(), rs -> {
            Long courseId = rs.getLong("course_id");
            Teacher teacher = new Teacher();
            teacher.setId(rs.getLong("id"));
            teacher.setName(rs.getString("name"));
            School school = new School();
            school.setId(rs.getLong("school_id"));
            teacher.setSchool(school);
            teacher.setCourses(new ArrayList<>());
            courses.stream()
                    .filter(c -> c.getId().equals(courseId))
                    .findFirst()
                    .ifPresent(c -> c.getTeachers().add(teacher));
            return null;
        });

        // Загрузка студентов для всех курсов
        String studentSql = "SELECT sc.course_id, s.id, s.name, s.school_id FROM students s " +
                "JOIN students_courses sc ON s.id = sc.student_id " +
                "WHERE sc.course_id IN (" + String.join(",", courses.stream().map(c -> "?").toList()) + ")";
        List<Long> studentParams = courses.stream().map(Course::getId).toList();
        jdbcTemplate.query(studentSql, studentParams.toArray(), rs -> {
            Long courseId = rs.getLong("course_id");
            Student student = new Student();
            student.setId(rs.getLong("id"));
            student.setName(rs.getString("name"));
            School school = new School();
            school.setId(rs.getLong("school_id"));
            student.setSchool(school);
            student.setCourses(new ArrayList<>());
            courses.stream()
                    .filter(c -> c.getId().equals(courseId))
                    .findFirst()
                    .ifPresent(c -> c.getStudents().add(student));
            return null;
        });

        return courses;
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
}