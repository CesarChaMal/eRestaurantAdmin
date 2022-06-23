package com.erestaurant.admin.service.impl;

import com.erestaurant.admin.domain.Admin;
import com.erestaurant.admin.repository.AdminRepository;
import com.erestaurant.admin.service.AdminService;
import com.erestaurant.admin.service.dto.AdminDTO;
import com.erestaurant.admin.service.mapper.AdminMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link Admin}.
 */
@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    private final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);

    private final AdminRepository adminRepository;

    private final AdminMapper adminMapper;

    public AdminServiceImpl(AdminRepository adminRepository, AdminMapper adminMapper) {
        this.adminRepository = adminRepository;
        this.adminMapper = adminMapper;
    }

    @Override
    public Mono<AdminDTO> save(AdminDTO adminDTO) {
        log.debug("Request to save Admin : {}", adminDTO);
        return adminRepository.save(adminMapper.toEntity(adminDTO)).map(adminMapper::toDto);
    }

    @Override
    public Mono<AdminDTO> update(AdminDTO adminDTO) {
        log.debug("Request to save Admin : {}", adminDTO);
        return adminRepository.save(adminMapper.toEntity(adminDTO).setIsPersisted()).map(adminMapper::toDto);
    }

    @Override
    public Mono<AdminDTO> partialUpdate(AdminDTO adminDTO) {
        log.debug("Request to partially update Admin : {}", adminDTO);

        return adminRepository
            .findById(adminDTO.getId())
            .map(existingAdmin -> {
                adminMapper.partialUpdate(existingAdmin, adminDTO);

                return existingAdmin;
            })
            .flatMap(adminRepository::save)
            .map(adminMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<AdminDTO> findAll() {
        log.debug("Request to get all Admins");
        return adminRepository.findAll().map(adminMapper::toDto);
    }

    public Mono<Long> countAll() {
        return adminRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<AdminDTO> findOne(String id) {
        log.debug("Request to get Admin : {}", id);
        return adminRepository.findById(id).map(adminMapper::toDto);
    }

    @Override
    public Mono<Void> delete(String id) {
        log.debug("Request to delete Admin : {}", id);
        return adminRepository.deleteById(id);
    }
}
