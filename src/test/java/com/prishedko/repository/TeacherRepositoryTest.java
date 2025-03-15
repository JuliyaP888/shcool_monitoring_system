package com.prishedko.repository;

import com.prishedko.entity.School;
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
class TeacherRepositoryTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    private TeacherRepository repository;
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

        // Инициализируем схему перед каждым тестом
        try (var statement = connection.createStatement()) {
            statement.execute(DROP_TABLES);
            statement.execute(CREATE_TABLES);
        }

        repository = new TeacherRepository(connection);
    }

    @Test
    void testSave() throws SQLException {
        Long schoolId = createSchool("Test School");
        School school = new School(schoolId, "Test School");

        Teacher teacher = new Teacher(null, "Test Teacher", school, null);
        Teacher savedTeacher = repository.save(teacher);

        assertNotNull(savedTeacher.getId());
        assertEquals("Test Teacher", savedTeacher.getName());
        assertEquals(schoolId, savedTeacher.getSchool().getId());
    }

    @Test
    void testFindById() throws SQLException {
        Long schoolId = createSchool("Test School");
        School school = new School(schoolId, "Test School");
        Teacher teacher = repository.save(new Teacher(null, "Test Teacher", school, null));

        Teacher foundTeacher = repository.findById(teacher.getId());

        assertNotNull(foundTeacher);
        assertEquals(teacher.getId(), foundTeacher.getId());
        assertEquals("Test Teacher", foundTeacher.getName());
        assertEquals(schoolId, foundTeacher.getSchool().getId());
        assertNotNull(foundTeacher.getCourses());
    }

    @Test
    void testFindByIdWithCourses() throws SQLException {
        Long schoolId = createSchool("Test School");
        School school = new School(schoolId, "Test School");
        Teacher teacher = repository.save(new Teacher(null, "Test Teacher", school, null));

        Long courseId = createCourse("Math");
        createTeacherCourseLink(teacher.getId(), courseId);

        Teacher foundTeacher = repository.findById(teacher.getId());
        assertEquals(1, foundTeacher.getCourses().size());
        assertEquals("Math", foundTeacher.getCourses().get(0).getName());
    }

    @Test
    void testFindByIdNotFound() throws SQLException {
        Teacher foundTeacher = repository.findById(999L);
        assertNull(foundTeacher);
    }

    @Test
    void testUpdate() throws SQLException {
        Long schoolId1 = createSchool("School 1");
        Long schoolId2 = createSchool("School 2");

        School school = new School(schoolId1, "School 1");
        Teacher teacher = repository.save(new Teacher(null, "Original Name", school, null));

        teacher.setName("Updated Name");
        teacher.setSchool(new School(schoolId2, "School 2"));
        Teacher updatedTeacher = repository.update(teacher);

        assertEquals("Updated Name", updatedTeacher.getName());
        assertEquals(schoolId2, updatedTeacher.getSchool().getId());

        Teacher foundTeacher = repository.findById(teacher.getId());
        assertEquals("Updated Name", foundTeacher.getName());
        assertEquals(schoolId2, foundTeacher.getSchool().getId());
    }

    @Test
    void testUpdateNotFound() throws SQLException {
        Long schoolId = createSchool("Test School");
        Teacher teacher = new Teacher(999L, "Non-existent", new School(schoolId), null);
        assertThrows(IllegalArgumentException.class, () -> repository.update(teacher));
    }

    @Test
    void testDelete() throws SQLException {
        Long schoolId = createSchool("Test School");
        School school = new School(schoolId, "Test School");
        Teacher teacher = repository.save(new Teacher(null, "Test Teacher", school, null));

        repository.delete(teacher.getId());

        Teacher foundTeacher = repository.findById(teacher.getId());
        assertNull(foundTeacher);
    }

    @Test
    void testDeleteNotFound() {
        assertThrows(IllegalArgumentException.class, () -> repository.delete(999L));
    }

    @Test
    void testFindBySchoolId() throws SQLException {
        Long schoolId = createSchool("Test School");
        School school = new School(schoolId, "Test School");

        Teacher teacher1 = repository.save(new Teacher(null, "Teacher 1", school, null));
        Teacher teacher2 = repository.save(new Teacher(null, "Teacher 2", school, null));

        Long courseId1 = createCourse("Math");
        Long courseId2 = createCourse("Physics");
        createTeacherCourseLink(teacher1.getId(), courseId1);
        createTeacherCourseLink(teacher2.getId(), courseId2);

        List<Teacher> teachers = repository.findBySchoolId(schoolId);
        assertEquals(2, teachers.size());

        Teacher foundTeacher1 = teachers.stream()
                .filter(t -> t.getName().equals("Teacher 1"))
                .findFirst()
                .orElseThrow();
        Teacher foundTeacher2 = teachers.stream()
                .filter(t -> t.getName().equals("Teacher 2"))
                .findFirst()
                .orElseThrow();

        assertEquals(1, foundTeacher1.getCourses().size());
        assertEquals("Math", foundTeacher1.getCourses().get(0).getName());
        assertEquals(1, foundTeacher2.getCourses().size());
        assertEquals("Physics", foundTeacher2.getCourses().get(0).getName());
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

    private Long createCourse(String name) throws SQLException {
        try (var ps = connection.prepareStatement(
                "INSERT INTO courses (name) VALUES (?) RETURNING id")) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getLong("id");
        }
    }

    private void createTeacherCourseLink(Long teacherId, Long courseId) throws SQLException {
        try (var ps = connection.prepareStatement(
                "INSERT INTO teachers_courses (teacher_id, course_id) VALUES (?, ?)")) {
            ps.setLong(1, teacherId);
            ps.setLong(2, courseId);
            ps.executeUpdate();
        }
    }
}
