package com.prishedko.repository;

import com.prishedko.entity.School;
import com.prishedko.entity.Student;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.prishedko.Util.CREATE_TABLES;
import static com.prishedko.Util.DROP_TABLES;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class StudentRepositoryTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15"))
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    private StudentRepository repository;
    private Connection connection;

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @BeforeEach
    void setUp() throws SQLException, NoSuchFieldException, IllegalAccessException {
        // Создаем соединение для вспомогательных методов
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

        // Создаем HikariDataSource для Testcontainers
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgres.getJdbcUrl());
        config.setUsername(postgres.getUsername());
        config.setPassword(postgres.getPassword());
        config.setMaximumPoolSize(10);
        HikariDataSource testDataSource = new HikariDataSource(config);

        // Используем рефлексию для замены dataSource в DatabaseConfig
        Field dataSourceField = com.prishedko.config.DatabaseConfig.class.getDeclaredField("dataSource");
        dataSourceField.setAccessible(true);
        dataSourceField.set(null, testDataSource);

        repository = new StudentRepository();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    void testSave() throws SQLException {
        Long schoolId = createSchool("Test School");
        School school = new School(schoolId, "Test School");

        Student student = new Student(null, "Test Student", school, null);
        Student savedStudent = repository.save(student);

        assertNotNull(savedStudent.getId());
        assertEquals("Test Student", savedStudent.getName());
        assertEquals(schoolId, savedStudent.getSchool().getId());
    }

    @Test
    void testFindById() throws SQLException {
        Long schoolId = createSchool("Test School");
        School school = new School(schoolId, "Test School");
        Student student = repository.save(new Student(null, "Test Student", school, null));

        Student foundStudent = repository.findById(student.getId());

        assertNotNull(foundStudent);
        assertEquals(student.getId(), foundStudent.getId());
        assertEquals("Test Student", foundStudent.getName());
        assertEquals(schoolId, foundStudent.getSchool().getId());
        assertNotNull(foundStudent.getCourses());
    }

    @Test
    void testFindByIdWithCourses() throws SQLException {
        Long schoolId = createSchool("Test School");
        School school = new School(schoolId, "Test School");

        Student student = repository.save(new Student(null, "Test Student", school, null));

        Long courseId = createCourse("Math");
        createStudentCourseLink(student.getId(), courseId);

        Student foundStudent = repository.findById(student.getId());
        assertEquals(1, foundStudent.getCourses().size());
        assertEquals("Math", foundStudent.getCourses().get(0).getName());
    }

    @Test
    void testFindByIdNotFound() throws SQLException {
        Student foundStudent = repository.findById(999L);
        assertNull(foundStudent);
    }

    @Test
    void testUpdate() throws SQLException {
        Long schoolId1 = createSchool("School 1");
        Long schoolId2 = createSchool("School 2");

        School school = new School(schoolId1, "School 1");
        Student student = repository.save(new Student(null, "Original Name", school, null));

        student.setName("Updated Name");
        student.setSchool(new School(schoolId2, "School 2"));
        Student updatedStudent = repository.update(student);

        assertEquals("Updated Name", updatedStudent.getName());
        assertEquals(schoolId2, updatedStudent.getSchool().getId());

        Student foundStudent = repository.findById(student.getId());
        assertEquals("Updated Name", foundStudent.getName());
        assertEquals(schoolId2, foundStudent.getSchool().getId());
    }

    @Test
    void testUpdateNotFound() throws SQLException {
        Long schoolId = createSchool("Test School");
        Student student = new Student(999L, "Non-existent", new School(schoolId, "Test School"), null);
        assertThrows(IllegalArgumentException.class, () -> repository.update(student));
    }

    @Test
    void testDelete() throws SQLException {
        Long schoolId = createSchool("Test School");
        School school = new School(schoolId, "Test School");
        Student student = repository.save(new Student(null, "Test Student", school, null));

        repository.delete(student.getId());

        Student foundStudent = repository.findById(student.getId());
        assertNull(foundStudent);
    }

    @Test
    void testDeleteNotFound() {
        assertThrows(IllegalArgumentException.class, () -> repository.delete(999L));
    }

    @Test
    void testFindBySchoolId() throws SQLException {
        Long schoolId = createSchool("Test School");
        School school = new School(schoolId, "Test School");

        repository.save(new Student(null, "Student 1", school, null));
        repository.save(new Student(null, "Student 2", school, null));

        List<Student> students = repository.findBySchoolId(schoolId);
        assertEquals(2, students.size());
        assertTrue(students.stream().anyMatch(s -> s.getName().equals("Student 1")));
        assertTrue(students.stream().anyMatch(s -> s.getName().equals("Student 2")));
    }

    // Вспомогательные методы для создания тестовых данных
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

    private void createStudentCourseLink(Long studentId, Long courseId) throws SQLException {
        try (var ps = connection.prepareStatement(
                "INSERT INTO students_courses (student_id, course_id) VALUES (?, ?)")) {
            ps.setLong(1, studentId);
            ps.setLong(2, courseId);
            ps.executeUpdate();
        }
    }
}