package com.prishedko.repository;

import com.prishedko.entity.School;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static com.prishedko.Util.CREATE_TABLES;
import static com.prishedko.Util.DROP_TABLES;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class SchoolRepositoryTest {
    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    private SchoolRepository repository;
    private Connection connection;

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @BeforeEach
    void setUp() throws SQLException {
        // Устанавливаем соединение с тестовой базой
        connection = DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
        );

        // Инициализируем схему перед каждым тестом
        try (var statement = connection.createStatement()) {
            // Очищаем таблицы
            statement.execute(DROP_TABLES);

            // Создаем таблицы
            statement.execute(CREATE_TABLES);
        }

        repository = new SchoolRepository(connection);
    }

    @Test
    void testFindById() throws SQLException {
        // Сначала сохраняем школу
        School school = repository.save(new School(1, "Test School"));

        // Затем находим её
        School foundSchool = repository.findById(school.getId());

        assertNotNull(foundSchool);
        assertEquals(school.getId(), foundSchool.getId());
        assertEquals("Test School", foundSchool.getName());
        assertNotNull(foundSchool.getTeachers());
        assertNotNull(foundSchool.getStudents());
    }

    @Test
    void testFindByIdNotFound() throws SQLException {
        School foundSchool = repository.findById(999L);
        assertNull(foundSchool);
    }

    @Test
    void testUpdate() throws SQLException {
        // Сохраняем школу
        School school = repository.save(new School(1, "Original Name"));

        // Обновляем
        school.setName("Updated Name");
        School updatedSchool = repository.update(school);

        assertEquals("Updated Name", updatedSchool.getName());

        // Проверяем в базе
        School foundSchool = repository.findById(school.getId());
        assertEquals("Updated Name", foundSchool.getName());
    }

    @Test
    void testUpdateNotFound() {
        School school = new School(999L, "Non-existent");
        assertThrows(IllegalArgumentException.class, () -> repository.update(school));
    }

    @Test
    void testExistsById() throws SQLException {
        School school = repository.save(new School(1, "Test School"));

        assertTrue(repository.existsById(school.getId()));
        assertFalse(repository.existsById(999L));
    }

    @Test
    void testDelete() throws SQLException {
        School school = repository.save(new School(1, "Test School"));

        repository.delete(school.getId());
        assertFalse(repository.existsById(school.getId()));
    }

    @Test
    void testDeleteNotFound() {
        assertThrows(IllegalArgumentException.class, () -> repository.delete(999L));
    }


    @Test
    void testFindByIdWithTeachersAndStudents() throws SQLException {
        // Сохраняем школу
        School school = repository.save(new School(1, "Test School"));

        // Добавляем учителя и студента напрямую в базу
        try (var ps = connection.prepareStatement(
                "INSERT INTO teachers (name, school_id) VALUES (?, ?)")) {
            ps.setString(1, "Teacher 1");
            ps.setLong(2, school.getId());
            ps.executeUpdate();
        }

        try (var ps = connection.prepareStatement(
                "INSERT INTO students (name, school_id) VALUES (?, ?)")) {
            ps.setString(1, "Student 1");
            ps.setLong(2, school.getId());
            ps.executeUpdate();
        }

        // Проверяем
        School foundSchool = repository.findById(school.getId());
        assertEquals(1, foundSchool.getTeachers().size());
        assertEquals("Teacher 1", foundSchool.getTeachers().get(0).getName());
        assertEquals(1, foundSchool.getStudents().size());
        assertEquals("Student 1", foundSchool.getStudents().get(0).getName());
    }
}