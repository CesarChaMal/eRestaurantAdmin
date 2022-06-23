package com.erestaurant.admin.web.rest;

import com.erestaurant.admin.repository.AppDiscountRepository;
import com.erestaurant.admin.service.AppDiscountService;
import com.erestaurant.admin.service.dto.AppDiscountDTO;
import com.erestaurant.admin.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link com.erestaurant.admin.domain.AppDiscount}.
 */
@RestController
@RequestMapping("/api")
public class AppDiscountResource {

    private final Logger log = LoggerFactory.getLogger(AppDiscountResource.class);

    private static final String ENTITY_NAME = "eRestaurantAdminAppDiscount";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AppDiscountService appDiscountService;

    private final AppDiscountRepository appDiscountRepository;

    public AppDiscountResource(AppDiscountService appDiscountService, AppDiscountRepository appDiscountRepository) {
        this.appDiscountService = appDiscountService;
        this.appDiscountRepository = appDiscountRepository;
    }

    /**
     * {@code POST  /app-discounts} : Create a new appDiscount.
     *
     * @param appDiscountDTO the appDiscountDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new appDiscountDTO, or with status {@code 400 (Bad Request)} if the appDiscount has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/app-discounts")
    public Mono<ResponseEntity<AppDiscountDTO>> createAppDiscount(@Valid @RequestBody AppDiscountDTO appDiscountDTO)
        throws URISyntaxException {
        log.debug("REST request to save AppDiscount : {}", appDiscountDTO);
        if (appDiscountDTO.getId() != null) {
            throw new BadRequestAlertException("A new appDiscount cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return appDiscountService
            .save(appDiscountDTO)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/app-discounts/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /app-discounts/:id} : Updates an existing appDiscount.
     *
     * @param id the id of the appDiscountDTO to save.
     * @param appDiscountDTO the appDiscountDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated appDiscountDTO,
     * or with status {@code 400 (Bad Request)} if the appDiscountDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the appDiscountDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/app-discounts/{id}")
    public Mono<ResponseEntity<AppDiscountDTO>> updateAppDiscount(
        @PathVariable(value = "id", required = false) final String id,
        @Valid @RequestBody AppDiscountDTO appDiscountDTO
    ) throws URISyntaxException {
        log.debug("REST request to update AppDiscount : {}, {}", id, appDiscountDTO);
        if (appDiscountDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, appDiscountDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return appDiscountRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return appDiscountService
                    .update(appDiscountDTO)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity
                            .ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId()))
                            .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /app-discounts/:id} : Partial updates given fields of an existing appDiscount, field will ignore if it is null
     *
     * @param id the id of the appDiscountDTO to save.
     * @param appDiscountDTO the appDiscountDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated appDiscountDTO,
     * or with status {@code 400 (Bad Request)} if the appDiscountDTO is not valid,
     * or with status {@code 404 (Not Found)} if the appDiscountDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the appDiscountDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/app-discounts/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<AppDiscountDTO>> partialUpdateAppDiscount(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody AppDiscountDTO appDiscountDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update AppDiscount partially : {}, {}", id, appDiscountDTO);
        if (appDiscountDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, appDiscountDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return appDiscountRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<AppDiscountDTO> result = appDiscountService.partialUpdate(appDiscountDTO);

                return result
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(res ->
                        ResponseEntity
                            .ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId()))
                            .body(res)
                    );
            });
    }

    /**
     * {@code GET  /app-discounts} : get all the appDiscounts.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of appDiscounts in body.
     */
    @GetMapping("/app-discounts")
    public Mono<List<AppDiscountDTO>> getAllAppDiscounts() {
        log.debug("REST request to get all AppDiscounts");
        return appDiscountService.findAll().collectList();
    }

    /**
     * {@code GET  /app-discounts} : get all the appDiscounts as a stream.
     * @return the {@link Flux} of appDiscounts.
     */
    @GetMapping(value = "/app-discounts", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<AppDiscountDTO> getAllAppDiscountsAsStream() {
        log.debug("REST request to get all AppDiscounts as a stream");
        return appDiscountService.findAll();
    }

    /**
     * {@code GET  /app-discounts/:id} : get the "id" appDiscount.
     *
     * @param id the id of the appDiscountDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the appDiscountDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/app-discounts/{id}")
    public Mono<ResponseEntity<AppDiscountDTO>> getAppDiscount(@PathVariable String id) {
        log.debug("REST request to get AppDiscount : {}", id);
        Mono<AppDiscountDTO> appDiscountDTO = appDiscountService.findOne(id);
        return ResponseUtil.wrapOrNotFound(appDiscountDTO);
    }

    /**
     * {@code DELETE  /app-discounts/:id} : delete the "id" appDiscount.
     *
     * @param id the id of the appDiscountDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/app-discounts/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deleteAppDiscount(@PathVariable String id) {
        log.debug("REST request to delete AppDiscount : {}", id);
        return appDiscountService
            .delete(id)
            .map(result ->
                ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id)).build()
            );
    }
}
