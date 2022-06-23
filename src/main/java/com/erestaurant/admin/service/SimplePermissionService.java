package com.erestaurant.admin.service;

import com.erestaurant.admin.service.dto.SimplePermissionDTO;
import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link com.erestaurant.admin.domain.SimplePermission}.
 */
public interface SimplePermissionService {
    /**
     * Save a simplePermission.
     *
     * @param simplePermissionDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<SimplePermissionDTO> save(SimplePermissionDTO simplePermissionDTO);

    /**
     * Updates a simplePermission.
     *
     * @param simplePermissionDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<SimplePermissionDTO> update(SimplePermissionDTO simplePermissionDTO);

    /**
     * Partially updates a simplePermission.
     *
     * @param simplePermissionDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<SimplePermissionDTO> partialUpdate(SimplePermissionDTO simplePermissionDTO);

    /**
     * Get all the simplePermissions.
     *
     * @return the list of entities.
     */
    Flux<SimplePermissionDTO> findAll();

    /**
     * Returns the number of simplePermissions available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Get the "id" simplePermission.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<SimplePermissionDTO> findOne(String id);

    /**
     * Delete the "id" simplePermission.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(String id);
}
