package com.erestaurant.admin.web.rest;

import com.erestaurant.admin.repository.CompositePermissionRepository;
import com.erestaurant.admin.service.CompositePermissionService;
import com.erestaurant.admin.service.dto.CompositePermissionDTO;
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
 * REST controller for managing {@link com.erestaurant.admin.domain.CompositePermission}.
 */
@RestController
@RequestMapping("/api")
public class CompositePermissionResource {

    private final Logger log = LoggerFactory.getLogger(CompositePermissionResource.class);

    private static final String ENTITY_NAME = "eRestaurantAdminCompositePermission";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CompositePermissionService compositePermissionService;

    private final CompositePermissionRepository compositePermissionRepository;

    public CompositePermissionResource(
        CompositePermissionService compositePermissionService,
        CompositePermissionRepository compositePermissionRepository
    ) {
        this.compositePermissionService = compositePermissionService;
        this.compositePermissionRepository = compositePermissionRepository;
    }

    /**
     * {@code POST  /composite-permissions} : Create a new compositePermission.
     *
     * @param compositePermissionDTO the compositePermissionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new compositePermissionDTO, or with status {@code 400 (Bad Request)} if the compositePermission has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/composite-permissions")
    public Mono<ResponseEntity<CompositePermissionDTO>> createCompositePermission(
        @Valid @RequestBody CompositePermissionDTO compositePermissionDTO
    ) throws URISyntaxException {
        log.debug("REST request to save CompositePermission : {}", compositePermissionDTO);
        if (compositePermissionDTO.getId() != null) {
            throw new BadRequestAlertException("A new compositePermission cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return compositePermissionService
            .save(compositePermissionDTO)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/composite-permissions/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /composite-permissions/:id} : Updates an existing compositePermission.
     *
     * @param id the id of the compositePermissionDTO to save.
     * @param compositePermissionDTO the compositePermissionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated compositePermissionDTO,
     * or with status {@code 400 (Bad Request)} if the compositePermissionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the compositePermissionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/composite-permissions/{id}")
    public Mono<ResponseEntity<CompositePermissionDTO>> updateCompositePermission(
        @PathVariable(value = "id", required = false) final String id,
        @Valid @RequestBody CompositePermissionDTO compositePermissionDTO
    ) throws URISyntaxException {
        log.debug("REST request to update CompositePermission : {}, {}", id, compositePermissionDTO);
        if (compositePermissionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, compositePermissionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return compositePermissionRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return compositePermissionService
                    .update(compositePermissionDTO)
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
     * {@code PATCH  /composite-permissions/:id} : Partial updates given fields of an existing compositePermission, field will ignore if it is null
     *
     * @param id the id of the compositePermissionDTO to save.
     * @param compositePermissionDTO the compositePermissionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated compositePermissionDTO,
     * or with status {@code 400 (Bad Request)} if the compositePermissionDTO is not valid,
     * or with status {@code 404 (Not Found)} if the compositePermissionDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the compositePermissionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/composite-permissions/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<CompositePermissionDTO>> partialUpdateCompositePermission(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody CompositePermissionDTO compositePermissionDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update CompositePermission partially : {}, {}", id, compositePermissionDTO);
        if (compositePermissionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, compositePermissionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return compositePermissionRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<CompositePermissionDTO> result = compositePermissionService.partialUpdate(compositePermissionDTO);

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
     * {@code GET  /composite-permissions} : get all the compositePermissions.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of compositePermissions in body.
     */
    @GetMapping("/composite-permissions")
    public Mono<List<CompositePermissionDTO>> getAllCompositePermissions() {
        log.debug("REST request to get all CompositePermissions");
        return compositePermissionService.findAll().collectList();
    }

    /**
     * {@code GET  /composite-permissions} : get all the compositePermissions as a stream.
     * @return the {@link Flux} of compositePermissions.
     */
    @GetMapping(value = "/composite-permissions", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<CompositePermissionDTO> getAllCompositePermissionsAsStream() {
        log.debug("REST request to get all CompositePermissions as a stream");
        return compositePermissionService.findAll();
    }

    /**
     * {@code GET  /composite-permissions/:id} : get the "id" compositePermission.
     *
     * @param id the id of the compositePermissionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the compositePermissionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/composite-permissions/{id}")
    public Mono<ResponseEntity<CompositePermissionDTO>> getCompositePermission(@PathVariable String id) {
        log.debug("REST request to get CompositePermission : {}", id);
        Mono<CompositePermissionDTO> compositePermissionDTO = compositePermissionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(compositePermissionDTO);
    }

    /**
     * {@code DELETE  /composite-permissions/:id} : delete the "id" compositePermission.
     *
     * @param id the id of the compositePermissionDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/composite-permissions/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deleteCompositePermission(@PathVariable String id) {
        log.debug("REST request to delete CompositePermission : {}", id);
        return compositePermissionService
            .delete(id)
            .map(result ->
                ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id)).build()
            );
    }
}
