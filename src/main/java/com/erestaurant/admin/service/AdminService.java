package com.erestaurant.admin.service;

import com.erestaurant.admin.service.dto.AdminDTO;
import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link com.erestaurant.admin.domain.Admin}.
 */
public interface AdminService {
    /**
     * Save a admin.
     *
     * @param adminDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<AdminDTO> save(AdminDTO adminDTO);

    /**
     * Updates a admin.
     *
     * @param adminDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<AdminDTO> update(AdminDTO adminDTO);

    /**
     * Partially updates a admin.
     *
     * @param adminDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<AdminDTO> partialUpdate(AdminDTO adminDTO);

    /**
     * Get all the admins.
     *
     * @return the list of entities.
     */
    Flux<AdminDTO> findAll();

    /**
     * Returns the number of admins available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Get the "id" admin.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<AdminDTO> findOne(String id);

    /**
     * Delete the "id" admin.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(String id);
}
