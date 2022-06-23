package com.erestaurant.admin.service;

import com.erestaurant.admin.service.dto.CompositePermissionDTO;
import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link com.erestaurant.admin.domain.CompositePermission}.
 */
public interface CompositePermissionService {
    /**
     * Save a compositePermission.
     *
     * @param compositePermissionDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<CompositePermissionDTO> save(CompositePermissionDTO compositePermissionDTO);

    /**
     * Updates a compositePermission.
     *
     * @param compositePermissionDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<CompositePermissionDTO> update(CompositePermissionDTO compositePermissionDTO);

    /**
     * Partially updates a compositePermission.
     *
     * @param compositePermissionDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<CompositePermissionDTO> partialUpdate(CompositePermissionDTO compositePermissionDTO);

    /**
     * Get all the compositePermissions.
     *
     * @return the list of entities.
     */
    Flux<CompositePermissionDTO> findAll();

    /**
     * Returns the number of compositePermissions available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Get the "id" compositePermission.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<CompositePermissionDTO> findOne(String id);

    /**
     * Delete the "id" compositePermission.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(String id);
}
