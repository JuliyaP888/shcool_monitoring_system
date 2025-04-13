package com.prishedko.service;

import com.prishedko.dto.SchoolDTO;
import com.prishedko.entity.School;
import com.prishedko.mapper.SchoolMapper;
import com.prishedko.repository.SchoolRepository;
import org.springframework.stereotype.Service;

@Service
public class SchoolService {

    private final SchoolRepository repository;

    public SchoolService(SchoolRepository repository) {
        this.repository = repository;
    }

    public SchoolDTO createSchool(SchoolDTO dto) {
        School school = SchoolMapper.INSTANCE.toEntity(dto);
        School saved = repository.save(school);
        return SchoolMapper.INSTANCE.toDTO(saved);
    }

    public SchoolDTO getSchool(Long id) {
        School school = repository.findById(id);
        if (school == null) {
            throw new IllegalArgumentException("School not found");
        }
        return SchoolMapper.INSTANCE.toDTO(school);
    }

    public void deleteSchool(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("School with id " + id + " not found");
        }
        repository.delete(id);
    }

    public SchoolDTO updateSchool(SchoolDTO dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("School ID cannot be null for update");
        }
        School school = SchoolMapper.INSTANCE.toEntity(dto);
        School updated = repository.update(school);
        return SchoolMapper.INSTANCE.toDTO(updated);
    }
}