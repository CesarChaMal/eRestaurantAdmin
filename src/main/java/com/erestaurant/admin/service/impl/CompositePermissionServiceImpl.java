package com.erestaurant.admin.service.impl;

import com.erestaurant.admin.domain.CompositePermission;
import com.erestaurant.admin.repository.CompositePermissionRepository;
import com.erestaurant.admin.service.CompositePermissionService;
import com.erestaurant.admin.service.dto.CompositePermissionDTO;
import com.erestaurant.admin.service.mapper.CompositePermissionMapper;
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
 * Service Implementation for managing {@link CompositePermission}.
 */
@Service
@Transactional
public class CompositePermissionServiceImpl implements CompositePermissionService {

    private final Logger log = LoggerFactory.getLogger(CompositePermissionServiceImpl.class);

    private final CompositePermissionRepository compositePermissionRepository;

    private final CompositePermissionMapper compositePermissionMapper;

    public CompositePermissionServiceImpl(
        CompositePermissionRepository compositePermissionRepository,
        CompositePermissionMapper compositePermissionMapper
    ) {
        this.compositePermissionRepository = compositePermissionRepository;
        this.compositePermissionMapper = compositePermissionMapper;
    }

    @Override
    public Mono<CompositePermissionDTO> save(CompositePermissionDTO compositePermissionDTO) {
        log.debug("Request to save CompositePermission : {}", compositePermissionDTO);
        return compositePermissionRepository
            .save(compositePermissionMapper.toEntity(compositePermissionDTO))
            .map(compositePermissionMapper::toDto);
    }

    @Override
    public Mono<CompositePermissionDTO> update(CompositePermissionDTO compositePermissionDTO) {
        log.debug("Request to save CompositePermission : {}", compositePermissionDTO);
        return compositePermissionRepository
            .save(compositePermissionMapper.toEntity(compositePermissionDTO).setIsPersisted())
            .map(compositePermissionMapper::toDto);
    }

    @Override
    public Mono<CompositePermissionDTO> partialUpdate(CompositePermissionDTO compositePermissionDTO) {
        log.debug("Request to partially update CompositePermission : {}", compositePermissionDTO);

        return compositePermissionRepository
            .findById(compositePermissionDTO.getId())
            .map(existingCompositePermission -> {
                compositePermissionMapper.partialUpdate(existingCompositePermission, compositePermissionDTO);

                return existingCompositePermission;
            })
            .flatMap(compositePermissionRepository::save)
            .map(compositePermissionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<CompositePermissionDTO> findAll() {
        log.debug("Request to get all CompositePermissions");
        return compositePermissionRepository.findAll().map(compositePermissionMapper::toDto);
    }

    public Mono<Long> countAll() {
        return compositePermissionRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<CompositePermissionDTO> findOne(String id) {
        log.debug("Request to get CompositePermission : {}", id);
        return compositePermissionRepository.findById(id).map(compositePermissionMapper::toDto);
    }

    @Override
    public Mono<Void> delete(String id) {
        log.debug("Request to delete CompositePermission : {}", id);
        return compositePermissionRepository.deleteById(id);
    }
}
