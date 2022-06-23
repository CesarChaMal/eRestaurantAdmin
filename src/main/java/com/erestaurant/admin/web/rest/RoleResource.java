package com.erestaurant.admin.web.rest;

import com.erestaurant.admin.repository.RoleRepository;
import com.erestaurant.admin.service.RoleService;
import com.erestaurant.admin.service.dto.RoleDTO;
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
 * REST controller for managing {@link com.erestaurant.admin.domain.Role}.
 */
@RestController
@RequestMapping("/api")
public class RoleResource {

    private final Logger log = LoggerFactory.getLogger(RoleResource.class);

    private static final String ENTITY_NAME = "eRestaurantAdminRole";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final RoleService roleService;

    private final RoleRepository roleRepository;

    public RoleResource(RoleService roleService, RoleRepository roleRepository) {
        this.roleService = roleService;
        this.roleRepository = roleRepository;
    }

    /**
     * {@code POST  /roles} : Create a new role.
     *
     * @param roleDTO the roleDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new roleDTO, or with status {@code 400 (Bad Request)} if the role has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/roles")
    public Mono<ResponseEntity<RoleDTO>> createRole(@Valid @RequestBody RoleDTO roleDTO) throws URISyntaxException {
        log.debug("REST request to save Role : {}", roleDTO);
        if (roleDTO.getId() != null) {
            throw new BadRequestAlertException("A new role cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return roleService
            .save(roleDTO)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/roles/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /roles/:id} : Updates an existing role.
     *
     * @param id the id of the roleDTO to save.
     * @param roleDTO the roleDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated roleDTO,
     * or with status {@code 400 (Bad Request)} if the roleDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the roleDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/roles/{id}")
    public Mono<ResponseEntity<RoleDTO>> updateRole(
        @PathVariable(value = "id", required = false) final String id,
        @Valid @RequestBody RoleDTO roleDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Role : {}, {}", id, roleDTO);
        if (roleDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, roleDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return roleRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return roleService
                    .update(roleDTO)
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
     * {@code PATCH  /roles/:id} : Partial updates given fields of an existing role, field will ignore if it is null
     *
     * @param id the id of the roleDTO to save.
     * @param roleDTO the roleDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated roleDTO,
     * or with status {@code 400 (Bad Request)} if the roleDTO is not valid,
     * or with status {@code 404 (Not Found)} if the roleDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the roleDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/roles/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<RoleDTO>> partialUpdateRole(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody RoleDTO roleDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Role partially : {}, {}", id, roleDTO);
        if (roleDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, roleDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return roleRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<RoleDTO> result = roleService.partialUpdate(roleDTO);

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
     * {@code GET  /roles} : get all the roles.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of roles in body.
     */
    @GetMapping("/roles")
    public Mono<List<RoleDTO>> getAllRoles() {
        log.debug("REST request to get all Roles");
        return roleService.findAll().collectList();
    }

    /**
     * {@code GET  /roles} : get all the roles as a stream.
     * @return the {@link Flux} of roles.
     */
    @GetMapping(value = "/roles", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<RoleDTO> getAllRolesAsStream() {
        log.debug("REST request to get all Roles as a stream");
        return roleService.findAll();
    }

    /**
     * {@code GET  /roles/:id} : get the "id" role.
     *
     * @param id the id of the roleDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the roleDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/roles/{id}")
    public Mono<ResponseEntity<RoleDTO>> getRole(@PathVariable String id) {
        log.debug("REST request to get Role : {}", id);
        Mono<RoleDTO> roleDTO = roleService.findOne(id);
        return ResponseUtil.wrapOrNotFound(roleDTO);
    }

    /**
     * {@code DELETE  /roles/:id} : delete the "id" role.
     *
     * @param id the id of the roleDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/roles/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deleteRole(@PathVariable String id) {
        log.debug("REST request to delete Role : {}", id);
        return roleService
            .delete(id)
            .map(result ->
                ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id)).build()
            );
    }
}
