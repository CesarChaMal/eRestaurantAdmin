package com.erestaurant.admin.web.rest;

import com.erestaurant.admin.repository.SimplePermissionRepository;
import com.erestaurant.admin.service.SimplePermissionService;
import com.erestaurant.admin.service.dto.SimplePermissionDTO;
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
 * REST controller for managing {@link com.erestaurant.admin.domain.SimplePermission}.
 */
@RestController
@RequestMapping("/api")
public class SimplePermissionResource {

    private final Logger log = LoggerFactory.getLogger(SimplePermissionResource.class);

    private static final String ENTITY_NAME = "eRestaurantAdminSimplePermission";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SimplePermissionService simplePermissionService;

    private final SimplePermissionRepository simplePermissionRepository;

    public SimplePermissionResource(
        SimplePermissionService simplePermissionService,
        SimplePermissionRepository simplePermissionRepository
    ) {
        this.simplePermissionService = simplePermissionService;
        this.simplePermissionRepository = simplePermissionRepository;
    }

    /**
     * {@code POST  /simple-permissions} : Create a new simplePermission.
     *
     * @param simplePermissionDTO the simplePermissionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new simplePermissionDTO, or with status {@code 400 (Bad Request)} if the simplePermission has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/simple-permissions")
    public Mono<ResponseEntity<SimplePermissionDTO>> createSimplePermission(@Valid @RequestBody SimplePermissionDTO simplePermissionDTO)
        throws URISyntaxException {
        log.debug("REST request to save SimplePermission : {}", simplePermissionDTO);
        if (simplePermissionDTO.getId() != null) {
            throw new BadRequestAlertException("A new simplePermission cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return simplePermissionService
            .save(simplePermissionDTO)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/simple-permissions/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /simple-permissions/:id} : Updates an existing simplePermission.
     *
     * @param id the id of the simplePermissionDTO to save.
     * @param simplePermissionDTO the simplePermissionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated simplePermissionDTO,
     * or with status {@code 400 (Bad Request)} if the simplePermissionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the simplePermissionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/simple-permissions/{id}")
    public Mono<ResponseEntity<SimplePermissionDTO>> updateSimplePermission(
        @PathVariable(value = "id", required = false) final String id,
        @Valid @RequestBody SimplePermissionDTO simplePermissionDTO
    ) throws URISyntaxException {
        log.debug("REST request to update SimplePermission : {}, {}", id, simplePermissionDTO);
        if (simplePermissionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, simplePermissionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return simplePermissionRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return simplePermissionService
                    .update(simplePermissionDTO)
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
     * {@code PATCH  /simple-permissions/:id} : Partial updates given fields of an existing simplePermission, field will ignore if it is null
     *
     * @param id the id of the simplePermissionDTO to save.
     * @param simplePermissionDTO the simplePermissionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated simplePermissionDTO,
     * or with status {@code 400 (Bad Request)} if the simplePermissionDTO is not valid,
     * or with status {@code 404 (Not Found)} if the simplePermissionDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the simplePermissionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/simple-permissions/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<SimplePermissionDTO>> partialUpdateSimplePermission(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody SimplePermissionDTO simplePermissionDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update SimplePermission partially : {}, {}", id, simplePermissionDTO);
        if (simplePermissionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, simplePermissionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return simplePermissionRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<SimplePermissionDTO> result = simplePermissionService.partialUpdate(simplePermissionDTO);

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
     * {@code GET  /simple-permissions} : get all the simplePermissions.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of simplePermissions in body.
     */
    @GetMapping("/simple-permissions")
    public Mono<List<SimplePermissionDTO>> getAllSimplePermissions() {
        log.debug("REST request to get all SimplePermissions");
        return simplePermissionService.findAll().collectList();
    }

    /**
     * {@code GET  /simple-permissions} : get all the simplePermissions as a stream.
     * @return the {@link Flux} of simplePermissions.
     */
    @GetMapping(value = "/simple-permissions", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<SimplePermissionDTO> getAllSimplePermissionsAsStream() {
        log.debug("REST request to get all SimplePermissions as a stream");
        return simplePermissionService.findAll();
    }

    /**
     * {@code GET  /simple-permissions/:id} : get the "id" simplePermission.
     *
     * @param id the id of the simplePermissionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the simplePermissionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/simple-permissions/{id}")
    public Mono<ResponseEntity<SimplePermissionDTO>> getSimplePermission(@PathVariable String id) {
        log.debug("REST request to get SimplePermission : {}", id);
        Mono<SimplePermissionDTO> simplePermissionDTO = simplePermissionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(simplePermissionDTO);
    }

    /**
     * {@code DELETE  /simple-permissions/:id} : delete the "id" simplePermission.
     *
     * @param id the id of the simplePermissionDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/simple-permissions/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deleteSimplePermission(@PathVariable String id) {
        log.debug("REST request to delete SimplePermission : {}", id);
        return simplePermissionService
            .delete(id)
            .map(result ->
                ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id)).build()
            );
    }
}
