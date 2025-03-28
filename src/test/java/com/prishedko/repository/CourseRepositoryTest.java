package com.prishedko.repository;

import com.prishedko.entity.Course;
import com.prishedko.entity.School;
import com.prishedko.entity.Student;
import com.prishedko.entity.Teacher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.prishedko.Util.CREATE_TABLES;
import static com.prishedko.Util.DROP_TABLES;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class CourseRepositoryTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    private CourseRepository repository;
    private Connection connection;

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
        );

        try (var statement = connection.createStatement()) {
            statement.execute(DROP_TABLES);
            statement.execute(CREATE_TABLES);
        }

        repository = new CourseRepository();
    }

    @Test
    void testSave() throws SQLException {
        Course course = new Course(null, "Test Course", null, null);
        Course savedCourse = repository.save(course);

        assertNotNull(savedCourse.getId());
        assertEquals("Test Course", savedCourse.getName());
    }

    @Test
    void testSaveWithTeachersAndStudents() throws SQLException {
        Long schoolId = createSchool("Test School");
        School school = new School(schoolId, "Test School");

        Long teacherId = createTeacher("Teacher 1", schoolId);
        Long studentId = createStudent("Student 1", schoolId);

        Teacher teacher = new Teacher(teacherId, "Teacher 1", school, null);
        Student student = new Student(studentId, "Student 1", school, null);

        Course course = new Course(null, "Test Course", List.of(teacher), List.of(student));
        Course savedCourse = repository.save(course);

        Course foundCourse = repository.findById(savedCourse.getId());
        assertEquals(1, foundCourse.getTeachers().size());
        assertEquals("Teacher 1", foundCourse.getTeachers().get(0).getName());
        assertEquals(1, foundCourse.getStudents().size());
        assertEquals("Student 1", foundCourse.getStudents().get(0).getName());
    }

    @Test
    void testFindById() throws SQLException {
        Course course = repository.save(new Course(null, "Test Course", null, null));
        Course foundCourse = repository.findById(course.getId());

        assertNotNull(foundCourse);
        assertEquals(course.getId(), foundCourse.getId());
        assertEquals("Test Course", foundCourse.getName());
        assertNotNull(foundCourse.getTeachers());
        assertNotNull(foundCourse.getStudents());
    }

    @Test
    void testFindByIdWithRelations() throws SQLException {
        Long schoolId = createSchool("Test School");
        School school = new School(schoolId, "Test School");

        Long teacherId = createTeacher("Teacher 1", schoolId);
        Long studentId = createStudent("Student 1", schoolId);

        Teacher teacher = new Teacher(teacherId, "Teacher 1", school, null);
        Student student = new Student(studentId, "Student 1", school, null);

        Course course = new Course(null, "Test Course", List.of(teacher), List.of(student));
        Course savedCourse = repository.save(course);

        Course foundCourse = repository.findById(savedCourse.getId());
        assertEquals(1, foundCourse.getTeachers().size());
        assertEquals("Teacher 1", foundCourse.getTeachers().get(0).getName());
        assertEquals(schoolId, foundCourse.getTeachers().get(0).getSchool().getId());
        assertEquals(1, foundCourse.getStudents().size());
        assertEquals("Student 1", foundCourse.getStudents().get(0).getName());
        assertEquals(schoolId, foundCourse.getStudents().get(0).getSchool().getId());
    }

    @Test
    void testFindByIdNotFound() throws SQLException {
        Course foundCourse = repository.findById(999L);
        assertNull(foundCourse);
    }

    @Test
    void testUpdate() throws SQLException {
        Course course = repository.save(new Course(null, "Original Name", null, null));
        course.setName("Updated Name");
        Course updatedCourse = repository.update(course);

        assertEquals("Updated Name", updatedCourse.getName());

        Course foundCourse = repository.findById(course.getId());
        assertEquals("Updated Name", foundCourse.getName());
    }

    @Test
    void testUpdateNotFound() {
        Course course = new Course(999L, "Non-existent", null, null);
        assertThrows(IllegalArgumentException.class, () -> repository.update(course));
    }

    @Test
    void testDelete() throws SQLException {
        Course course = repository.save(new Course(null, "Test Course", null, null));
        repository.delete(course.getId());

        Course foundCourse = repository.findById(course.getId());
        assertNull(foundCourse);
    }

    @Test
    void testDeleteNotFound() {
        assertThrows(IllegalArgumentException.class, () -> repository.delete(999L));
    }

    @Test
    void testFindAll() throws SQLException {
        repository.save(new Course(null, "Course 1", null, null));
        repository.save(new Course(null, "Course 2", null, null));

        List<Course> courses = repository.findAll();
        assertEquals(2, courses.size());
        assertTrue(courses.stream().anyMatch(c -> c.getName().equals("Course 1")));
        assertTrue(courses.stream().anyMatch(c -> c.getName().equals("Course 2"))


        );
    }

    // Вспомогательные методы
    private Long createSchool(String name) throws SQLException {
        try (var ps = connection.prepareStatement(
                "INSERT INTO schools (name) VALUES (?) RETURNING id")) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getLong("id");
        }
    }

    private Long createTeacher(String name, Long schoolId) throws SQLException {
        try (var ps = connection.prepareStatement(
                "INSERT INTO teachers (name, school_id) VALUES (?, ?) RETURNING id")) {
            ps.setString(1, name);
            ps.setLong(2, schoolId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getLong("id");
        }
    }

    private Long createStudent(String name, Long schoolId) throws SQLException {
        try (var ps = connection.prepareStatement(
                "INSERT INTO students (name, school_id) VALUES (?, ?) RETURNING id")) {
            ps.setString(1, name);
            ps.setLong(2, schoolId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getLong("id");
        }
    }
}
