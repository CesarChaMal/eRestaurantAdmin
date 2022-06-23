package com.erestaurant.admin.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.erestaurant.admin.IntegrationTest;
import com.erestaurant.admin.domain.CompositePermission;
import com.erestaurant.admin.repository.CompositePermissionRepository;
import com.erestaurant.admin.repository.EntityManager;
import com.erestaurant.admin.service.dto.CompositePermissionDTO;
import com.erestaurant.admin.service.mapper.CompositePermissionMapper;
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
 * Integration tests for the {@link CompositePermissionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class CompositePermissionResourceIT {

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/composite-permissions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private CompositePermissionRepository compositePermissionRepository;

    @Autowired
    private CompositePermissionMapper compositePermissionMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private CompositePermission compositePermission;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CompositePermission createEntity(EntityManager em) {
        CompositePermission compositePermission = new CompositePermission().description(DEFAULT_DESCRIPTION);
        return compositePermission;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CompositePermission createUpdatedEntity(EntityManager em) {
        CompositePermission compositePermission = new CompositePermission().description(UPDATED_DESCRIPTION);
        return compositePermission;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(CompositePermission.class).block();
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
        compositePermission = createEntity(em);
    }

    @Test
    void createCompositePermission() throws Exception {
        int databaseSizeBeforeCreate = compositePermissionRepository.findAll().collectList().block().size();
        // Create the CompositePermission
        CompositePermissionDTO compositePermissionDTO = compositePermissionMapper.toDto(compositePermission);
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(compositePermissionDTO))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the CompositePermission in the database
        List<CompositePermission> compositePermissionList = compositePermissionRepository.findAll().collectList().block();
        assertThat(compositePermissionList).hasSize(databaseSizeBeforeCreate + 1);
        CompositePermission testCompositePermission = compositePermissionList.get(compositePermissionList.size() - 1);
        assertThat(testCompositePermission.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    void createCompositePermissionWithExistingId() throws Exception {
        // Create the CompositePermission with an existing ID
        compositePermission.setId("existing_id");
        CompositePermissionDTO compositePermissionDTO = compositePermissionMapper.toDto(compositePermission);

        int databaseSizeBeforeCreate = compositePermissionRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(compositePermissionDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the CompositePermission in the database
        List<CompositePermission> compositePermissionList = compositePermissionRepository.findAll().collectList().block();
        assertThat(compositePermissionList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void getAllCompositePermissionsAsStream() {
        // Initialize the database
        compositePermission.setId(UUID.randomUUID().toString());
        compositePermissionRepository.save(compositePermission).block();

        List<CompositePermission> compositePermissionList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(CompositePermissionDTO.class)
            .getResponseBody()
            .map(compositePermissionMapper::toEntity)
            .filter(compositePermission::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(compositePermissionList).isNotNull();
        assertThat(compositePermissionList).hasSize(1);
        CompositePermission testCompositePermission = compositePermissionList.get(0);
        assertThat(testCompositePermission.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    void getAllCompositePermissions() {
        // Initialize the database
        compositePermission.setId(UUID.randomUUID().toString());
        compositePermissionRepository.save(compositePermission).block();

        // Get all the compositePermissionList
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
            .value(hasItem(compositePermission.getId()))
            .jsonPath("$.[*].description")
            .value(hasItem(DEFAULT_DESCRIPTION.toString()));
    }

    @Test
    void getCompositePermission() {
        // Initialize the database
        compositePermission.setId(UUID.randomUUID().toString());
        compositePermissionRepository.save(compositePermission).block();

        // Get the compositePermission
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, compositePermission.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(compositePermission.getId()))
            .jsonPath("$.description")
            .value(is(DEFAULT_DESCRIPTION.toString()));
    }

    @Test
    void getNonExistingCompositePermission() {
        // Get the compositePermission
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewCompositePermission() throws Exception {
        // Initialize the database
        compositePermission.setId(UUID.randomUUID().toString());
        compositePermissionRepository.save(compositePermission).block();

        int databaseSizeBeforeUpdate = compositePermissionRepository.findAll().collectList().block().size();

        // Update the compositePermission
        CompositePermission updatedCompositePermission = compositePermissionRepository.findById(compositePermission.getId()).block();
        updatedCompositePermission.description(UPDATED_DESCRIPTION);
        CompositePermissionDTO compositePermissionDTO = compositePermissionMapper.toDto(updatedCompositePermission);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, compositePermissionDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(compositePermissionDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the CompositePermission in the database
        List<CompositePermission> compositePermissionList = compositePermissionRepository.findAll().collectList().block();
        assertThat(compositePermissionList).hasSize(databaseSizeBeforeUpdate);
        CompositePermission testCompositePermission = compositePermissionList.get(compositePermissionList.size() - 1);
        assertThat(testCompositePermission.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    void putNonExistingCompositePermission() throws Exception {
        int databaseSizeBeforeUpdate = compositePermissionRepository.findAll().collectList().block().size();
        compositePermission.setId(UUID.randomUUID().toString());

        // Create the CompositePermission
        CompositePermissionDTO compositePermissionDTO = compositePermissionMapper.toDto(compositePermission);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, compositePermissionDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(compositePermissionDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the CompositePermission in the database
        List<CompositePermission> compositePermissionList = compositePermissionRepository.findAll().collectList().block();
        assertThat(compositePermissionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchCompositePermission() throws Exception {
        int databaseSizeBeforeUpdate = compositePermissionRepository.findAll().collectList().block().size();
        compositePermission.setId(UUID.randomUUID().toString());

        // Create the CompositePermission
        CompositePermissionDTO compositePermissionDTO = compositePermissionMapper.toDto(compositePermission);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(compositePermissionDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the CompositePermission in the database
        List<CompositePermission> compositePermissionList = compositePermissionRepository.findAll().collectList().block();
        assertThat(compositePermissionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamCompositePermission() throws Exception {
        int databaseSizeBeforeUpdate = compositePermissionRepository.findAll().collectList().block().size();
        compositePermission.setId(UUID.randomUUID().toString());

        // Create the CompositePermission
        CompositePermissionDTO compositePermissionDTO = compositePermissionMapper.toDto(compositePermission);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(compositePermissionDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the CompositePermission in the database
        List<CompositePermission> compositePermissionList = compositePermissionRepository.findAll().collectList().block();
        assertThat(compositePermissionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateCompositePermissionWithPatch() throws Exception {
        // Initialize the database
        compositePermission.setId(UUID.randomUUID().toString());
        compositePermissionRepository.save(compositePermission).block();

        int databaseSizeBeforeUpdate = compositePermissionRepository.findAll().collectList().block().size();

        // Update the compositePermission using partial update
        CompositePermission partialUpdatedCompositePermission = new CompositePermission();
        partialUpdatedCompositePermission.setId(compositePermission.getId());

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedCompositePermission.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedCompositePermission))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the CompositePermission in the database
        List<CompositePermission> compositePermissionList = compositePermissionRepository.findAll().collectList().block();
        assertThat(compositePermissionList).hasSize(databaseSizeBeforeUpdate);
        CompositePermission testCompositePermission = compositePermissionList.get(compositePermissionList.size() - 1);
        assertThat(testCompositePermission.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    void fullUpdateCompositePermissionWithPatch() throws Exception {
        // Initialize the database
        compositePermission.setId(UUID.randomUUID().toString());
        compositePermissionRepository.save(compositePermission).block();

        int databaseSizeBeforeUpdate = compositePermissionRepository.findAll().collectList().block().size();

        // Update the compositePermission using partial update
        CompositePermission partialUpdatedCompositePermission = new CompositePermission();
        partialUpdatedCompositePermission.setId(compositePermission.getId());

        partialUpdatedCompositePermission.description(UPDATED_DESCRIPTION);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedCompositePermission.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedCompositePermission))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the CompositePermission in the database
        List<CompositePermission> compositePermissionList = compositePermissionRepository.findAll().collectList().block();
        assertThat(compositePermissionList).hasSize(databaseSizeBeforeUpdate);
        CompositePermission testCompositePermission = compositePermissionList.get(compositePermissionList.size() - 1);
        assertThat(testCompositePermission.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    void patchNonExistingCompositePermission() throws Exception {
        int databaseSizeBeforeUpdate = compositePermissionRepository.findAll().collectList().block().size();
        compositePermission.setId(UUID.randomUUID().toString());

        // Create the CompositePermission
        CompositePermissionDTO compositePermissionDTO = compositePermissionMapper.toDto(compositePermission);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, compositePermissionDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(compositePermissionDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the CompositePermission in the database
        List<CompositePermission> compositePermissionList = compositePermissionRepository.findAll().collectList().block();
        assertThat(compositePermissionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchCompositePermission() throws Exception {
        int databaseSizeBeforeUpdate = compositePermissionRepository.findAll().collectList().block().size();
        compositePermission.setId(UUID.randomUUID().toString());

        // Create the CompositePermission
        CompositePermissionDTO compositePermissionDTO = compositePermissionMapper.toDto(compositePermission);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(compositePermissionDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the CompositePermission in the database
        List<CompositePermission> compositePermissionList = compositePermissionRepository.findAll().collectList().block();
        assertThat(compositePermissionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamCompositePermission() throws Exception {
        int databaseSizeBeforeUpdate = compositePermissionRepository.findAll().collectList().block().size();
        compositePermission.setId(UUID.randomUUID().toString());

        // Create the CompositePermission
        CompositePermissionDTO compositePermissionDTO = compositePermissionMapper.toDto(compositePermission);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(compositePermissionDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the CompositePermission in the database
        List<CompositePermission> compositePermissionList = compositePermissionRepository.findAll().collectList().block();
        assertThat(compositePermissionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteCompositePermission() {
        // Initialize the database
        compositePermission.setId(UUID.randomUUID().toString());
        compositePermissionRepository.save(compositePermission).block();

        int databaseSizeBeforeDelete = compositePermissionRepository.findAll().collectList().block().size();

        // Delete the compositePermission
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, compositePermission.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<CompositePermission> compositePermissionList = compositePermissionRepository.findAll().collectList().block();
        assertThat(compositePermissionList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
