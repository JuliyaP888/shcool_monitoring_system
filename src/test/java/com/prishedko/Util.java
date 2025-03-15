package com.prishedko;

public class Util {
    public static final String DROP_TABLES = "DROP TABLE IF EXISTS students_courses, teachers_courses, students, teachers, courses, schools CASCADE";
    public static final String CREATE_TABLES = """
                CREATE TABLE schools (
                    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                    name VARCHAR(255) NOT NULL
                );
            
                CREATE TABLE teachers (
                    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                    name VARCHAR(255) NOT NULL,
                    school_id BIGINT NOT NULL,
                    CONSTRAINT fk_teacher_school FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE
                );
            
                CREATE TABLE students (
                    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                    name VARCHAR(255) NOT NULL,
                    school_id BIGINT NOT NULL,
                    CONSTRAINT fk_student_school FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE
                );
            
                CREATE TABLE courses (
                    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                    name VARCHAR(255) NOT NULL
                );
            
                CREATE TABLE teachers_courses (
                    teacher_id BIGINT NOT NULL,
                    course_id BIGINT NOT NULL,
                    PRIMARY KEY (teacher_id, course_id),
                    CONSTRAINT fk_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE CASCADE,
                    CONSTRAINT fk_course_teacher FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
                );
            
                CREATE TABLE students_courses (
                    student_id BIGINT NOT NULL,
                    course_id BIGINT NOT NULL,
                    PRIMARY KEY (student_id, course_id),
                    CONSTRAINT fk_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
                    CONSTRAINT fk_course_student FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
                );
            """;
}
