CREATE TABLE schools (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE teachers (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(255) NOT NULL,
    school_id BIGINT NOT NULL,
    CONSTRAINT fk_teacher_school
        FOREIGN KEY (school_id)
        REFERENCES schools(id)
        ON DELETE CASCADE
);

CREATE TABLE students (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(255) NOT NULL,
    school_id BIGINT NOT NULL,
    CONSTRAINT fk_student_school
        FOREIGN KEY (school_id)
        REFERENCES schools(id)
        ON DELETE CASCADE
);

CREATE TABLE courses (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(255) NOT NULL
);

-- (ManyToMany связь)
CREATE TABLE teachers_courses (
    teacher_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    PRIMARY KEY (teacher_id, course_id),
    CONSTRAINT fk_teacher
        FOREIGN KEY (teacher_id)
        REFERENCES teachers(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_course_teacher
        FOREIGN KEY (course_id)
        REFERENCES courses(id)
        ON DELETE CASCADE
);

-- (ManyToMany связь)
CREATE TABLE students_courses (
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    PRIMARY KEY (student_id, course_id),
    CONSTRAINT fk_student
        FOREIGN KEY (student_id)
        REFERENCES students(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_course_student
        FOREIGN KEY (course_id)
        REFERENCES courses(id)
        ON DELETE CASCADE
);

-- Создание индексов
CREATE INDEX idx_teachers_school_id ON teachers(school_id);
CREATE INDEX idx_students_school_id ON students(school_id);
CREATE INDEX idx_teachers_courses_teacher_id ON teachers_courses(teacher_id);
CREATE INDEX idx_teachers_courses_course_id ON teachers_courses(course_id);
CREATE INDEX idx_students_courses_student_id ON students_courses(student_id);
CREATE INDEX idx_students_courses_course_id ON students_courses(course_id);