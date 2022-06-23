package com.erestaurant.admin.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.erestaurant.admin.IntegrationTest;
import com.erestaurant.admin.domain.Permission;
import com.erestaurant.admin.repository.EntityManager;
import com.erestaurant.admin.repository.PermissionRepository;
import com.erestaurant.admin.service.dto.PermissionDTO;
import com.erestaurant.admin.service.mapper.PermissionMapper;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.Base64Utils;

/**
 * Integration tests for the {@link PermissionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class PermissionResourceIT {

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/permissions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Permission permission;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Permission createEntity(EntityManager em) {
        Permission permission = new Permission().description(DEFAULT_DESCRIPTION);
        return permission;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Permission createUpdatedEntity(EntityManager em) {
        Permission permission = new Permission().description(UPDATED_DESCRIPTION);
        return permission;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Permission.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @AfterEach
    public void cleanup() {
        deleteEntities(em);
    }

    @BeforeEach
    public void setupCsrf() {
        webTestClient = webTestClient.mutateWith(csrf());
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        permission = createEntity(em);
    }

    @Test
    void createPermission() throws Exception {
        int databaseSizeBeforeCreate = permissionRepository.findAll().collectList().block().size();
        // Create the Permission
        PermissionDTO permissionDTO = permissionMapper.toDto(permission);
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(permissionDTO))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Permission in the database
        List<Permission> permissionList = permissionRepository.findAll().collectList().block();
        assertThat(permissionList).hasSize(databaseSizeBeforeCreate + 1);
        Permission testPermission = permissionList.get(permissionList.size() - 1);
        assertThat(testPermission.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    void createPermissionWithExistingId() throws Exception {
        // Create the Permission with an existing ID
        permission.setId("existing_id");
        PermissionDTO permissionDTO = permissionMapper.toDto(permission);

        int databaseSizeBeforeCreate = permissionRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(permissionDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Permission in the database
        List<Permission> permissionList = permissionRepository.findAll().collectList().block();
        assertThat(permissionList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void getAllPermissionsAsStream() {
        // Initialize the database
        permission.setId(UUID.randomUUID().toString());
        permissionRepository.save(permission).block();

        List<Permission> permissionList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(PermissionDTO.class)
            .getResponseBody()
            .map(permissionMapper::toEntity)
            .filter(permission::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(permissionList).isNotNull();
        assertThat(permissionList).hasSize(1);
        Permission testPermission = permissionList.get(0);
        assertThat(testPermission.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    void getAllPermissions() {
        // Initialize the database
        permission.setId(UUID.randomUUID().toString());
        permissionRepository.save(permission).block();

        // Get all the permissionList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(permission.getId()))
            .jsonPath("$.[*].description")
            .value(hasItem(DEFAULT_DESCRIPTION.toString()));
    }

    @Test
    void getPermission() {
        // Initialize the database
        permission.setId(UUID.randomUUID().toString());
        permissionRepository.save(permission).block();

        // Get the permission
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, permission.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(permission.getId()))
            .jsonPath("$.description")
            .value(is(DEFAULT_DESCRIPTION.toString()));
    }

    @Test
    void getNonExistingPermission() {
        // Get the permission
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewPermission() throws Exception {
        // Initialize the database
        permission.setId(UUID.randomUUID().toString());
        permissionRepository.save(permission).block();

        int databaseSizeBeforeUpdate = permissionRepository.findAll().collectList().block().size();

        // Update the permission
        Permission updatedPermission = permissionRepository.findById(permission.getId()).block();
        updatedPermission.description(UPDATED_DESCRIPTION);
        PermissionDTO permissionDTO = permissionMapper.toDto(updatedPermission);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, permissionDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(permissionDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Permission in the database
        List<Permission> permissionList = permissionRepository.findAll().collectList().block();
        assertThat(permissionList).hasSize(databaseSizeBeforeUpdate);
        Permission testPermission = permissionList.get(permissionList.size() - 1);
        assertThat(testPermission.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    void putNonExistingPermission() throws Exception {
        int databaseSizeBeforeUpdate = permissionRepository.findAll().collectList().block().size();
        permission.setId(UUID.randomUUID().toString());

        // Create the Permission
        PermissionDTO permissionDTO = permissionMapper.toDto(permission);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, permissionDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(permissionDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Permission in the database
        List<Permission> permissionList = permissionRepository.findAll().collectList().block();
        assertThat(permissionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchPermission() throws Exception {
        int databaseSizeBeforeUpdate = permissionRepository.findAll().collectList().block().size();
        permission.setId(UUID.randomUUID().toString());

        // Create the Permission
        PermissionDTO permissionDTO = permissionMapper.toDto(permission);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(permissionDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Permission in the database
        List<Permission> permissionList = permissionRepository.findAll().collectList().block();
        assertThat(permissionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamPermission() throws Exception {
        int databaseSizeBeforeUpdate = permissionRepository.findAll().collectList().block().size();
        permission.setId(UUID.randomUUID().toString());

        // Create the Permission
        PermissionDTO permissionDTO = permissionMapper.toDto(permission);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(permissionDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Permission in the database
        List<Permission> permissionList = permissionRepository.findAll().collectList().block();
        assertThat(permissionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdatePermissionWithPatch() throws Exception {
        // Initialize the database
        permission.setId(UUID.randomUUID().toString());
        permissionRepository.save(permission).block();

        int databaseSizeBeforeUpdate = permissionRepository.findAll().collectList().block().size();

        // Update the permission using partial update
        Permission partialUpdatedPermission = new Permission();
        partialUpdatedPermission.setId(permission.getId());

        partialUpdatedPermission.description(UPDATED_DESCRIPTION);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedPermission.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedPermission))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Permission in the database
        List<Permission> permissionList = permissionRepository.findAll().collectList().block();
        assertThat(permissionList).hasSize(databaseSizeBeforeUpdate);
        Permission testPermission = permissionList.get(permissionList.size() - 1);
        assertThat(testPermission.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    void fullUpdatePermissionWithPatch() throws Exception {
        // Initialize the database
        permission.setId(UUID.randomUUID().toString());
        permissionRepository.save(permission).block();

        int databaseSizeBeforeUpdate = permissionRepository.findAll().collectList().block().size();

        // Update the permission using partial update
        Permission partialUpdatedPermission = new Permission();
        partialUpdatedPermission.setId(permission.getId());

        partialUpdatedPermission.description(UPDATED_DESCRIPTION);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedPermission.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedPermission))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Permission in the database
        List<Permission> permissionList = permissionRepository.findAll().collectList().block();
        assertThat(permissionList).hasSize(databaseSizeBeforeUpdate);
        Permission testPermission = permissionList.get(permissionList.size() - 1);
        assertThat(testPermission.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    void patchNonExistingPermission() throws Exception {
        int databaseSizeBeforeUpdate = permissionRepository.findAll().collectList().block().size();
        permission.setId(UUID.randomUUID().toString());

        // Create the Permission
        PermissionDTO permissionDTO = permissionMapper.toDto(permission);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, permissionDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(permissionDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Permission in the database
        List<Permission> permissionList = permissionRepository.findAll().collectList().block();
        assertThat(permissionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchPermission() throws Exception {
        int databaseSizeBeforeUpdate = permissionRepository.findAll().collectList().block().size();
        permission.setId(UUID.randomUUID().toString());

        // Create the Permission
        PermissionDTO permissionDTO = permissionMapper.toDto(permission);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(permissionDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Permission in the database
        List<Permission> permissionList = permissionRepository.findAll().collectList().block();
        assertThat(permissionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamPermission() throws Exception {
        int databaseSizeBeforeUpdate = permissionRepository.findAll().collectList().block().size();
        permission.setId(UUID.randomUUID().toString());

        // Create the Permission
        PermissionDTO permissionDTO = permissionMapper.toDto(permission);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(permissionDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Permission in the database
        List<Permission> permissionList = permissionRepository.findAll().collectList().block();
        assertThat(permissionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deletePermission() {
        // Initialize the database
        permission.setId(UUID.randomUUID().toString());
        permissionRepository.save(permission).block();

        int databaseSizeBeforeDelete = permissionRepository.findAll().collectList().block().size();

        // Delete the permission
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, permission.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Permission> permissionList = permissionRepository.findAll().collectList().block();
        assertThat(permissionList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
