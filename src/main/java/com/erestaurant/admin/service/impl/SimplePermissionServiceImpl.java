package com.erestaurant.admin.service.impl;

import com.erestaurant.admin.domain.SimplePermission;
import com.erestaurant.admin.repository.SimplePermissionRepository;
import com.erestaurant.admin.service.SimplePermissionService;
import com.erestaurant.admin.service.dto.SimplePermissionDTO;
import com.erestaurant.admin.service.mapper.SimplePermissionMapper;
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
 * Service Implementation for managing {@link SimplePermission}.
 */
@Service
@Transactional
public class SimplePermissionServiceImpl implements SimplePermissionService {

    private final Logger log = LoggerFactory.getLogger(SimplePermissionServiceImpl.class);

    private final SimplePermissionRepository simplePermissionRepository;

    private final SimplePermissionMapper simplePermissionMapper;

    public SimplePermissionServiceImpl(
        SimplePermissionRepository simplePermissionRepository,
        SimplePermissionMapper simplePermissionMapper
    ) {
        this.simplePermissionRepository = simplePermissionRepository;
        this.simplePermissionMapper = simplePermissionMapper;
    }

    @Override
    public Mono<SimplePermissionDTO> save(SimplePermissionDTO simplePermissionDTO) {
        log.debug("Request to save SimplePermission : {}", simplePermissionDTO);
        return simplePermissionRepository.save(simplePermissionMapper.toEntity(simplePermissionDTO)).map(simplePermissionMapper::toDto);
    }

    @Override
    public Mono<SimplePermissionDTO> update(SimplePermissionDTO simplePermissionDTO) {
        log.debug("Request to save SimplePermission : {}", simplePermissionDTO);
        return simplePermissionRepository
            .save(simplePermissionMapper.toEntity(simplePermissionDTO).setIsPersisted())
            .map(simplePermissionMapper::toDto);
    }

    @Override
    public Mono<SimplePermissionDTO> partialUpdate(SimplePermissionDTO simplePermissionDTO) {
        log.debug("Request to partially update SimplePermission : {}", simplePermissionDTO);

        return simplePermissionRepository
            .findById(simplePermissionDTO.getId())
            .map(existingSimplePermission -> {
                simplePermissionMapper.partialUpdate(existingSimplePermission, simplePermissionDTO);

                return existingSimplePermission;
            })
            .flatMap(simplePermissionRepository::save)
            .map(simplePermissionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<SimplePermissionDTO> findAll() {
        log.debug("Request to get all SimplePermissions");
        return simplePermissionRepository.findAll().map(simplePermissionMapper::toDto);
    }

    public Mono<Long> countAll() {
        return simplePermissionRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<SimplePermissionDTO> findOne(String id) {
        log.debug("Request to get SimplePermission : {}", id);
        return simplePermissionRepository.findById(id).map(simplePermissionMapper::toDto);
    }

    @Override
    public Mono<Void> delete(String id) {
        log.debug("Request to delete SimplePermission : {}", id);
        return simplePermissionRepository.deleteById(id);
    }
}
