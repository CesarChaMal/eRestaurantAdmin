package com.erestaurant.admin.service;

import com.erestaurant.admin.service.dto.DiscountDTO;
import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link com.erestaurant.admin.domain.Discount}.
 */
public interface DiscountService {
    /**
     * Save a discount.
     *
     * @param discountDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<DiscountDTO> save(DiscountDTO discountDTO);

    /**
     * Updates a discount.
     *
     * @param discountDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<DiscountDTO> update(DiscountDTO discountDTO);

    /**
     * Partially updates a discount.
     *
     * @param discountDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<DiscountDTO> partialUpdate(DiscountDTO discountDTO);

    /**
     * Get all the discounts.
     *
     * @return the list of entities.
     */
    Flux<DiscountDTO> findAll();

    /**
     * Returns the number of discounts available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Get the "id" discount.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<DiscountDTO> findOne(String id);

    /**
     * Delete the "id" discount.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(String id);
}
