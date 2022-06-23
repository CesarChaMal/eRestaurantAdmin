package com.erestaurant.admin.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.erestaurant.admin.IntegrationTest;
import com.erestaurant.admin.domain.SimplePermission;
import com.erestaurant.admin.repository.EntityManager;
import com.erestaurant.admin.repository.SimplePermissionRepository;
import com.erestaurant.admin.service.dto.SimplePermissionDTO;
import com.erestaurant.admin.service.mapper.SimplePermissionMapper;
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
 * Integration tests for the {@link SimplePermissionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class SimplePermissionResourceIT {

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/simple-permissions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private SimplePermissionRepository simplePermissionRepository;

    @Autowired
    private SimplePermissionMapper simplePermissionMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private SimplePermission simplePermission;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SimplePermission createEntity(EntityManager em) {
        SimplePermission simplePermission = new SimplePermission().description(DEFAULT_DESCRIPTION);
        return simplePermission;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SimplePermission createUpdatedEntity(EntityManager em) {
        SimplePermission simplePermission = new SimplePermission().description(UPDATED_DESCRIPTION);
        return simplePermission;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(SimplePermission.class).block();
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
        simplePermission = createEntity(em);
    }

    @Test
    void createSimplePermission() throws Exception {
        int databaseSizeBeforeCreate = simplePermissionRepository.findAll().collectList().block().size();
        // Create the SimplePermission
        SimplePermissionDTO simplePermissionDTO = simplePermissionMapper.toDto(simplePermission);
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(simplePermissionDTO))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the SimplePermission in the database
        List<SimplePermission> simplePermissionList = simplePermissionRepository.findAll().collectList().block();
        assertThat(simplePermissionList).hasSize(databaseSizeBeforeCreate + 1);
        SimplePermission testSimplePermission = simplePermissionList.get(simplePermissionList.size() - 1);
        assertThat(testSimplePermission.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    void createSimplePermissionWithExistingId() throws Exception {
        // Create the SimplePermission with an existing ID
        simplePermission.setId("existing_id");
        SimplePermissionDTO simplePermissionDTO = simplePermissionMapper.toDto(simplePermission);

        int databaseSizeBeforeCreate = simplePermissionRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(simplePermissionDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the SimplePermission in the database
        List<SimplePermission> simplePermissionList = simplePermissionRepository.findAll().collectList().block();
        assertThat(simplePermissionList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void getAllSimplePermissionsAsStream() {
        // Initialize the database
        simplePermission.setId(UUID.randomUUID().toString());
        simplePermissionRepository.save(simplePermission).block();

        List<SimplePermission> simplePermissionList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(SimplePermissionDTO.class)
            .getResponseBody()
            .map(simplePermissionMapper::toEntity)
            .filter(simplePermission::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(simplePermissionList).isNotNull();
        assertThat(simplePermissionList).hasSize(1);
        SimplePermission testSimplePermission = simplePermissionList.get(0);
        assertThat(testSimplePermission.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    void getAllSimplePermissions() {
        // Initialize the database
        simplePermission.setId(UUID.randomUUID().toString());
        simplePermissionRepository.save(simplePermission).block();

        // Get all the simplePermissionList
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
            .value(hasItem(simplePermission.getId()))
            .jsonPath("$.[*].description")
            .value(hasItem(DEFAULT_DESCRIPTION.toString()));
    }

    @Test
    void getSimplePermission() {
        // Initialize the database
        simplePermission.setId(UUID.randomUUID().toString());
        simplePermissionRepository.save(simplePermission).block();

        // Get the simplePermission
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, simplePermission.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(simplePermission.getId()))
            .jsonPath("$.description")
            .value(is(DEFAULT_DESCRIPTION.toString()));
    }

    @Test
    void getNonExistingSimplePermission() {
        // Get the simplePermission
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewSimplePermission() throws Exception {
        // Initialize the database
        simplePermission.setId(UUID.randomUUID().toString());
        simplePermissionRepository.save(simplePermission).block();

        int databaseSizeBeforeUpdate = simplePermissionRepository.findAll().collectList().block().size();

        // Update the simplePermission
        SimplePermission updatedSimplePermission = simplePermissionRepository.findById(simplePermission.getId()).block();
        updatedSimplePermission.description(UPDATED_DESCRIPTION);
        SimplePermissionDTO simplePermissionDTO = simplePermissionMapper.toDto(updatedSimplePermission);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, simplePermissionDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(simplePermissionDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the SimplePermission in the database
        List<SimplePermission> simplePermissionList = simplePermissionRepository.findAll().collectList().block();
        assertThat(simplePermissionList).hasSize(databaseSizeBeforeUpdate);
        SimplePermission testSimplePermission = simplePermissionList.get(simplePermissionList.size() - 1);
        assertThat(testSimplePermission.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    void putNonExistingSimplePermission() throws Exception {
        int databaseSizeBeforeUpdate = simplePermissionRepository.findAll().collectList().block().size();
        simplePermission.setId(UUID.randomUUID().toString());

        // Create the SimplePermission
        SimplePermissionDTO simplePermissionDTO = simplePermissionMapper.toDto(simplePermission);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, simplePermissionDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(simplePermissionDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the SimplePermission in the database
        List<SimplePermission> simplePermissionList = simplePermissionRepository.findAll().collectList().block();
        assertThat(simplePermissionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchSimplePermission() throws Exception {
        int databaseSizeBeforeUpdate = simplePermissionRepository.findAll().collectList().block().size();
        simplePermission.setId(UUID.randomUUID().toString());

        // Create the SimplePermission
        SimplePermissionDTO simplePermissionDTO = simplePermissionMapper.toDto(simplePermission);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(simplePermissionDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the SimplePermission in the database
        List<SimplePermission> simplePermissionList = simplePermissionRepository.findAll().collectList().block();
        assertThat(simplePermissionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamSimplePermission() throws Exception {
        int databaseSizeBeforeUpdate = simplePermissionRepository.findAll().collectList().block().size();
        simplePermission.setId(UUID.randomUUID().toString());

        // Create the SimplePermission
        SimplePermissionDTO simplePermissionDTO = simplePermissionMapper.toDto(simplePermission);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(simplePermissionDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the SimplePermission in the database
        List<SimplePermission> simplePermissionList = simplePermissionRepository.findAll().collectList().block();
        assertThat(simplePermissionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateSimplePermissionWithPatch() throws Exception {
        // Initialize the database
        simplePermission.setId(UUID.randomUUID().toString());
        simplePermissionRepository.save(simplePermission).block();

        int databaseSizeBeforeUpdate = simplePermissionRepository.findAll().collectList().block().size();

        // Update the simplePermission using partial update
        SimplePermission partialUpdatedSimplePermission = new SimplePermission();
        partialUpdatedSimplePermission.setId(simplePermission.getId());

        partialUpdatedSimplePermission.description(UPDATED_DESCRIPTION);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedSimplePermission.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedSimplePermission))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the SimplePermission in the database
        List<SimplePermission> simplePermissionList = simplePermissionRepository.findAll().collectList().block();
        assertThat(simplePermissionList).hasSize(databaseSizeBeforeUpdate);
        SimplePermission testSimplePermission = simplePermissionList.get(simplePermissionList.size() - 1);
        assertThat(testSimplePermission.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    void fullUpdateSimplePermissionWithPatch() throws Exception {
        // Initialize the database
        simplePermission.setId(UUID.randomUUID().toString());
        simplePermissionRepository.save(simplePermission).block();

        int databaseSizeBeforeUpdate = simplePermissionRepository.findAll().collectList().block().size();

        // Update the simplePermission using partial update
        SimplePermission partialUpdatedSimplePermission = new SimplePermission();
        partialUpdatedSimplePermission.setId(simplePermission.getId());

        partialUpdatedSimplePermission.description(UPDATED_DESCRIPTION);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedSimplePermission.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedSimplePermission))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the SimplePermission in the database
        List<SimplePermission> simplePermissionList = simplePermissionRepository.findAll().collectList().block();
        assertThat(simplePermissionList).hasSize(databaseSizeBeforeUpdate);
        SimplePermission testSimplePermission = simplePermissionList.get(simplePermissionList.size() - 1);
        assertThat(testSimplePermission.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    void patchNonExistingSimplePermission() throws Exception {
        int databaseSizeBeforeUpdate = simplePermissionRepository.findAll().collectList().block().size();
        simplePermission.setId(UUID.randomUUID().toString());

        // Create the SimplePermission
        SimplePermissionDTO simplePermissionDTO = simplePermissionMapper.toDto(simplePermission);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, simplePermissionDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(simplePermissionDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the SimplePermission in the database
        List<SimplePermission> simplePermissionList = simplePermissionRepository.findAll().collectList().block();
        assertThat(simplePermissionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchSimplePermission() throws Exception {
        int databaseSizeBeforeUpdate = simplePermissionRepository.findAll().collectList().block().size();
        simplePermission.setId(UUID.randomUUID().toString());

        // Create the SimplePermission
        SimplePermissionDTO simplePermissionDTO = simplePermissionMapper.toDto(simplePermission);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(simplePermissionDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the SimplePermission in the database
        List<SimplePermission> simplePermissionList = simplePermissionRepository.findAll().collectList().block();
        assertThat(simplePermissionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamSimplePermission() throws Exception {
        int databaseSizeBeforeUpdate = simplePermissionRepository.findAll().collectList().block().size();
        simplePermission.setId(UUID.randomUUID().toString());

        // Create the SimplePermission
        SimplePermissionDTO simplePermissionDTO = simplePermissionMapper.toDto(simplePermission);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(simplePermissionDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the SimplePermission in the database
        List<SimplePermission> simplePermissionList = simplePermissionRepository.findAll().collectList().block();
        assertThat(simplePermissionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteSimplePermission() {
        // Initialize the database
        simplePermission.setId(UUID.randomUUID().toString());
        simplePermissionRepository.save(simplePermission).block();

        int databaseSizeBeforeDelete = simplePermissionRepository.findAll().collectList().block().size();

        // Delete the simplePermission
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, simplePermission.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<SimplePermission> simplePermissionList = simplePermissionRepository.findAll().collectList().block();
        assertThat(simplePermissionList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
