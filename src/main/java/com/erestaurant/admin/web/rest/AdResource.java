package com.erestaurant.admin.web.rest;

import com.erestaurant.admin.repository.AdRepository;
import com.erestaurant.admin.service.AdService;
import com.erestaurant.admin.service.dto.AdDTO;
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
 * REST controller for managing {@link com.erestaurant.admin.domain.Ad}.
 */
@RestController
@RequestMapping("/api")
public class AdResource {

    private final Logger log = LoggerFactory.getLogger(AdResource.class);

    private static final String ENTITY_NAME = "eRestaurantAdminAd";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AdService adService;

    private final AdRepository adRepository;

    public AdResource(AdService adService, AdRepository adRepository) {
        this.adService = adService;
        this.adRepository = adRepository;
    }

    /**
     * {@code POST  /ads} : Create a new ad.
     *
     * @param adDTO the adDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new adDTO, or with status {@code 400 (Bad Request)} if the ad has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/ads")
    public Mono<ResponseEntity<AdDTO>> createAd(@Valid @RequestBody AdDTO adDTO) throws URISyntaxException {
        log.debug("REST request to save Ad : {}", adDTO);
        if (adDTO.getId() != null) {
            throw new BadRequestAlertException("A new ad cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return adService
            .save(adDTO)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/ads/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /ads/:id} : Updates an existing ad.
     *
     * @param id the id of the adDTO to save.
     * @param adDTO the adDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated adDTO,
     * or with status {@code 400 (Bad Request)} if the adDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the adDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/ads/{id}")
    public Mono<ResponseEntity<AdDTO>> updateAd(
        @PathVariable(value = "id", required = false) final String id,
        @Valid @RequestBody AdDTO adDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Ad : {}, {}", id, adDTO);
        if (adDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, adDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return adRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return adService
                    .update(adDTO)
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
     * {@code PATCH  /ads/:id} : Partial updates given fields of an existing ad, field will ignore if it is null
     *
     * @param id the id of the adDTO to save.
     * @param adDTO the adDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated adDTO,
     * or with status {@code 400 (Bad Request)} if the adDTO is not valid,
     * or with status {@code 404 (Not Found)} if the adDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the adDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/ads/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<AdDTO>> partialUpdateAd(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody AdDTO adDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Ad partially : {}, {}", id, adDTO);
        if (adDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, adDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return adRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<AdDTO> result = adService.partialUpdate(adDTO);

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
     * {@code GET  /ads} : get all the ads.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of ads in body.
     */
    @GetMapping("/ads")
    public Mono<List<AdDTO>> getAllAds() {
        log.debug("REST request to get all Ads");
        return adService.findAll().collectList();
    }

    /**
     * {@code GET  /ads} : get all the ads as a stream.
     * @return the {@link Flux} of ads.
     */
    @GetMapping(value = "/ads", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<AdDTO> getAllAdsAsStream() {
        log.debug("REST request to get all Ads as a stream");
        return adService.findAll();
    }

    /**
     * {@code GET  /ads/:id} : get the "id" ad.
     *
     * @param id the id of the adDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the adDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/ads/{id}")
    public Mono<ResponseEntity<AdDTO>> getAd(@PathVariable String id) {
        log.debug("REST request to get Ad : {}", id);
        Mono<AdDTO> adDTO = adService.findOne(id);
        return ResponseUtil.wrapOrNotFound(adDTO);
    }

    /**
     * {@code DELETE  /ads/:id} : delete the "id" ad.
     *
     * @param id the id of the adDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/ads/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deleteAd(@PathVariable String id) {
        log.debug("REST request to delete Ad : {}", id);
        return adService
            .delete(id)
            .map(result ->
                ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id)).build()
            );
    }
}
