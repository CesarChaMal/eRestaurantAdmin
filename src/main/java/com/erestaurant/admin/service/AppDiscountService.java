package com.erestaurant.admin.service;

import com.erestaurant.admin.service.dto.AppDiscountDTO;
import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link com.erestaurant.admin.domain.AppDiscount}.
 */
public interface AppDiscountService {
    /**
     * Save a appDiscount.
     *
     * @param appDiscountDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<AppDiscountDTO> save(AppDiscountDTO appDiscountDTO);

    /**
     * Updates a appDiscount.
     *
     * @param appDiscountDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<AppDiscountDTO> update(AppDiscountDTO appDiscountDTO);

    /**
     * Partially updates a appDiscount.
     *
     * @param appDiscountDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<AppDiscountDTO> partialUpdate(AppDiscountDTO appDiscountDTO);

    /**
     * Get all the appDiscounts.
     *
     * @return the list of entities.
     */
    Flux<AppDiscountDTO> findAll();

    /**
     * Returns the number of appDiscounts available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Get the "id" appDiscount.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<AppDiscountDTO> findOne(String id);

    /**
     * Delete the "id" appDiscount.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(String id);
}
