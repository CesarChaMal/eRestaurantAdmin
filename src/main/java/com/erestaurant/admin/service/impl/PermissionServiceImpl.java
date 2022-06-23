package com.erestaurant.admin.service.impl;

import com.erestaurant.admin.domain.Permission;
import com.erestaurant.admin.repository.PermissionRepository;
import com.erestaurant.admin.service.PermissionService;
import com.erestaurant.admin.service.dto.PermissionDTO;
import com.erestaurant.admin.service.mapper.PermissionMapper;
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
 * Service Implementation for managing {@link Permission}.
 */
@Service
@Transactional
public class PermissionServiceImpl implements PermissionService {

    private final Logger log = LoggerFactory.getLogger(PermissionServiceImpl.class);

    private final PermissionRepository permissionRepository;

    private final PermissionMapper permissionMapper;

    public PermissionServiceImpl(PermissionRepository permissionRepository, PermissionMapper permissionMapper) {
        this.permissionRepository = permissionRepository;
        this.permissionMapper = permissionMapper;
    }

    @Override
    public Mono<PermissionDTO> save(PermissionDTO permissionDTO) {
        log.debug("Request to save Permission : {}", permissionDTO);
        return permissionRepository.save(permissionMapper.toEntity(permissionDTO)).map(permissionMapper::toDto);
    }

    @Override
    public Mono<PermissionDTO> update(PermissionDTO permissionDTO) {
        log.debug("Request to save Permission : {}", permissionDTO);
        return permissionRepository.save(permissionMapper.toEntity(permissionDTO).setIsPersisted()).map(permissionMapper::toDto);
    }

    @Override
    public Mono<PermissionDTO> partialUpdate(PermissionDTO permissionDTO) {
        log.debug("Request to partially update Permission : {}", permissionDTO);

        return permissionRepository
            .findById(permissionDTO.getId())
            .map(existingPermission -> {
                permissionMapper.partialUpdate(existingPermission, permissionDTO);

                return existingPermission;
            })
            .flatMap(permissionRepository::save)
            .map(permissionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<PermissionDTO> findAll() {
        log.debug("Request to get all Permissions");
        return permissionRepository.findAll().map(permissionMapper::toDto);
    }

    public Mono<Long> countAll() {
        return permissionRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<PermissionDTO> findOne(String id) {
        log.debug("Request to get Permission : {}", id);
        return permissionRepository.findById(id).map(permissionMapper::toDto);
    }

    @Override
    public Mono<Void> delete(String id) {
        log.debug("Request to delete Permission : {}", id);
        return permissionRepository.deleteById(id);
    }
}
