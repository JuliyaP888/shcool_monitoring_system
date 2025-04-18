package com.prishedko.service;

import com.prishedko.dto.SchoolDTO;
import com.prishedko.entity.School;
import com.prishedko.mapper.SchoolMapper;
import com.prishedko.repository.SchoolRepository;

import java.sql.SQLException;

public class SchoolService {
    private final SchoolRepository repository;

    public SchoolService(SchoolRepository repository) {
        this.repository = repository;
    }

    public SchoolDTO createSchool(SchoolDTO dto) throws SQLException {
        School school = new School();
        school.setName(dto.getName());
        School saved = repository.save(school);
        return SchoolMapper.mapToDTO(saved);
    }

    public SchoolDTO getSchool(Long id) throws SQLException {
        School school = repository.findById(id);
        if (school == null) {
            throw new IllegalArgumentException("School not found");
        }
        return SchoolMapper.mapToDTO(school);
    }

    public void deleteSchool(Long id) throws SQLException {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("School with id " + id + " not found");
        }
        repository.delete(id);
    }

    public SchoolDTO updateSchool(SchoolDTO dto) throws SQLException {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("School ID cannot be null for update");
        }

        School school = new School();
        school.setId(dto.getId());
        school.setName(dto.getName());

        School updated = repository.update(school);
        return SchoolMapper.mapToDTO(updated);
    }
}