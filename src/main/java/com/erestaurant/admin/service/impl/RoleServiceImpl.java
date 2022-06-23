package com.erestaurant.admin.service.impl;

import com.erestaurant.admin.domain.Role;
import com.erestaurant.admin.repository.RoleRepository;
import com.erestaurant.admin.service.RoleService;
import com.erestaurant.admin.service.dto.RoleDTO;
import com.erestaurant.admin.service.mapper.RoleMapper;
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
 * Service Implementation for managing {@link Role}.
 */
@Service
@Transactional
public class RoleServiceImpl implements RoleService {

    private final Logger log = LoggerFactory.getLogger(RoleServiceImpl.class);

    private final RoleRepository roleRepository;

    private final RoleMapper roleMapper;

    public RoleServiceImpl(RoleRepository roleRepository, RoleMapper roleMapper) {
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
    }

    @Override
    public Mono<RoleDTO> save(RoleDTO roleDTO) {
        log.debug("Request to save Role : {}", roleDTO);
        return roleRepository.save(roleMapper.toEntity(roleDTO)).map(roleMapper::toDto);
    }

    @Override
    public Mono<RoleDTO> update(RoleDTO roleDTO) {
        log.debug("Request to save Role : {}", roleDTO);
        return roleRepository.save(roleMapper.toEntity(roleDTO).setIsPersisted()).map(roleMapper::toDto);
    }

    @Override
    public Mono<RoleDTO> partialUpdate(RoleDTO roleDTO) {
        log.debug("Request to partially update Role : {}", roleDTO);

        return roleRepository
            .findById(roleDTO.getId())
            .map(existingRole -> {
                roleMapper.partialUpdate(existingRole, roleDTO);

                return existingRole;
            })
            .flatMap(roleRepository::save)
            .map(roleMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<RoleDTO> findAll() {
        log.debug("Request to get all Roles");
        return roleRepository.findAll().map(roleMapper::toDto);
    }

    public Mono<Long> countAll() {
        return roleRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<RoleDTO> findOne(String id) {
        log.debug("Request to get Role : {}", id);
        return roleRepository.findById(id).map(roleMapper::toDto);
    }

    @Override
    public Mono<Void> delete(String id) {
        log.debug("Request to delete Role : {}", id);
        return roleRepository.deleteById(id);
    }
}
