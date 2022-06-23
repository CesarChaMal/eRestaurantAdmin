package com.erestaurant.admin.web.rest;

import com.erestaurant.admin.repository.AdminRepository;
import com.erestaurant.admin.service.AdminService;
import com.erestaurant.admin.service.dto.AdminDTO;
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
 * REST controller for managing {@link com.erestaurant.admin.domain.Admin}.
 */
@RestController
@RequestMapping("/api")
public class AdminResource {

    private final Logger log = LoggerFactory.getLogger(AdminResource.class);

    private static final String ENTITY_NAME = "eRestaurantAdminAdmin";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AdminService adminService;

    private final AdminRepository adminRepository;

    public AdminResource(AdminService adminService, AdminRepository adminRepository) {
        this.adminService = adminService;
        this.adminRepository = adminRepository;
    }

    /**
     * {@code POST  /admins} : Create a new admin.
     *
     * @param adminDTO the adminDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new adminDTO, or with status {@code 400 (Bad Request)} if the admin has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/admins")
    public Mono<ResponseEntity<AdminDTO>> createAdmin(@Valid @RequestBody AdminDTO adminDTO) throws URISyntaxException {
        log.debug("REST request to save Admin : {}", adminDTO);
        if (adminDTO.getId() != null) {
            throw new BadRequestAlertException("A new admin cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return adminService
            .save(adminDTO)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/admins/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /admins/:id} : Updates an existing admin.
     *
     * @param id the id of the adminDTO to save.
     * @param adminDTO the adminDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated adminDTO,
     * or with status {@code 400 (Bad Request)} if the adminDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the adminDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/admins/{id}")
    public Mono<ResponseEntity<AdminDTO>> updateAdmin(
        @PathVariable(value = "id", required = false) final String id,
        @Valid @RequestBody AdminDTO adminDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Admin : {}, {}", id, adminDTO);
        if (adminDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, adminDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return adminRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return adminService
                    .update(adminDTO)
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
     * {@code PATCH  /admins/:id} : Partial updates given fields of an existing admin, field will ignore if it is null
     *
     * @param id the id of the adminDTO to save.
     * @param adminDTO the adminDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated adminDTO,
     * or with status {@code 400 (Bad Request)} if the adminDTO is not valid,
     * or with status {@code 404 (Not Found)} if the adminDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the adminDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/admins/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<AdminDTO>> partialUpdateAdmin(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody AdminDTO adminDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Admin partially : {}, {}", id, adminDTO);
        if (adminDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, adminDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return adminRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<AdminDTO> result = adminService.partialUpdate(adminDTO);

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
     * {@code GET  /admins} : get all the admins.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of admins in body.
     */
    @GetMapping("/admins")
    public Mono<List<AdminDTO>> getAllAdmins() {
        log.debug("REST request to get all Admins");
        return adminService.findAll().collectList();
    }

    /**
     * {@code GET  /admins} : get all the admins as a stream.
     * @return the {@link Flux} of admins.
     */
    @GetMapping(value = "/admins", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<AdminDTO> getAllAdminsAsStream() {
        log.debug("REST request to get all Admins as a stream");
        return adminService.findAll();
    }

    /**
     * {@code GET  /admins/:id} : get the "id" admin.
     *
     * @param id the id of the adminDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the adminDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/admins/{id}")
    public Mono<ResponseEntity<AdminDTO>> getAdmin(@PathVariable String id) {
        log.debug("REST request to get Admin : {}", id);
        Mono<AdminDTO> adminDTO = adminService.findOne(id);
        return ResponseUtil.wrapOrNotFound(adminDTO);
    }

    /**
     * {@code DELETE  /admins/:id} : delete the "id" admin.
     *
     * @param id the id of the adminDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/admins/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deleteAdmin(@PathVariable String id) {
        log.debug("REST request to delete Admin : {}", id);
        return adminService
            .delete(id)
            .map(result ->
                ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id)).build()
            );
    }
}
