package com.erestaurant.admin.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.erestaurant.admin.IntegrationTest;
import com.erestaurant.admin.domain.Role;
import com.erestaurant.admin.repository.EntityManager;
import com.erestaurant.admin.repository.RoleRepository;
import com.erestaurant.admin.service.dto.RoleDTO;
import com.erestaurant.admin.service.mapper.RoleMapper;
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
 * Integration tests for the {@link RoleResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class RoleResourceIT {

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/roles";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Role role;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Role createEntity(EntityManager em) {
        Role role = new Role().description(DEFAULT_DESCRIPTION);
        return role;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Role createUpdatedEntity(EntityManager em) {
        Role role = new Role().description(UPDATED_DESCRIPTION);
        return role;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Role.class).block();
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
        role = createEntity(em);
    }

    @Test
    void createRole() throws Exception {
        int databaseSizeBeforeCreate = roleRepository.findAll().collectList().block().size();
        // Create the Role
        RoleDTO roleDTO = roleMapper.toDto(role);
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(roleDTO))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Role in the database
        List<Role> roleList = roleRepository.findAll().collectList().block();
        assertThat(roleList).hasSize(databaseSizeBeforeCreate + 1);
        Role testRole = roleList.get(roleList.size() - 1);
        assertThat(testRole.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    void createRoleWithExistingId() throws Exception {
        // Create the Role with an existing ID
        role.setId("existing_id");
        RoleDTO roleDTO = roleMapper.toDto(role);

        int databaseSizeBeforeCreate = roleRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(roleDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Role in the database
        List<Role> roleList = roleRepository.findAll().collectList().block();
        assertThat(roleList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void getAllRolesAsStream() {
        // Initialize the database
        role.setId(UUID.randomUUID().toString());
        roleRepository.save(role).block();

        List<Role> roleList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(RoleDTO.class)
            .getResponseBody()
            .map(roleMapper::toEntity)
            .filter(role::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(roleList).isNotNull();
        assertThat(roleList).hasSize(1);
        Role testRole = roleList.get(0);
        assertThat(testRole.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    void getAllRoles() {
        // Initialize the database
        role.setId(UUID.randomUUID().toString());
        roleRepository.save(role).block();

        // Get all the roleList
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
            .value(hasItem(role.getId()))
            .jsonPath("$.[*].description")
            .value(hasItem(DEFAULT_DESCRIPTION.toString()));
    }

    @Test
    void getRole() {
        // Initialize the database
        role.setId(UUID.randomUUID().toString());
        roleRepository.save(role).block();

        // Get the role
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, role.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(role.getId()))
            .jsonPath("$.description")
            .value(is(DEFAULT_DESCRIPTION.toString()));
    }

    @Test
    void getNonExistingRole() {
        // Get the role
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewRole() throws Exception {
        // Initialize the database
        role.setId(UUID.randomUUID().toString());
        roleRepository.save(role).block();

        int databaseSizeBeforeUpdate = roleRepository.findAll().collectList().block().size();

        // Update the role
        Role updatedRole = roleRepository.findById(role.getId()).block();
        updatedRole.description(UPDATED_DESCRIPTION);
        RoleDTO roleDTO = roleMapper.toDto(updatedRole);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, roleDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(roleDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Role in the database
        List<Role> roleList = roleRepository.findAll().collectList().block();
        assertThat(roleList).hasSize(databaseSizeBeforeUpdate);
        Role testRole = roleList.get(roleList.size() - 1);
        assertThat(testRole.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    void putNonExistingRole() throws Exception {
        int databaseSizeBeforeUpdate = roleRepository.findAll().collectList().block().size();
        role.setId(UUID.randomUUID().toString());

        // Create the Role
        RoleDTO roleDTO = roleMapper.toDto(role);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, roleDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(roleDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Role in the database
        List<Role> roleList = roleRepository.findAll().collectList().block();
        assertThat(roleList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchRole() throws Exception {
        int databaseSizeBeforeUpdate = roleRepository.findAll().collectList().block().size();
        role.setId(UUID.randomUUID().toString());

        // Create the Role
        RoleDTO roleDTO = roleMapper.toDto(role);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(roleDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Role in the database
        List<Role> roleList = roleRepository.findAll().collectList().block();
        assertThat(roleList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamRole() throws Exception {
        int databaseSizeBeforeUpdate = roleRepository.findAll().collectList().block().size();
        role.setId(UUID.randomUUID().toString());

        // Create the Role
        RoleDTO roleDTO = roleMapper.toDto(role);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(roleDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Role in the database
        List<Role> roleList = roleRepository.findAll().collectList().block();
        assertThat(roleList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateRoleWithPatch() throws Exception {
        // Initialize the database
        role.setId(UUID.randomUUID().toString());
        roleRepository.save(role).block();

        int databaseSizeBeforeUpdate = roleRepository.findAll().collectList().block().size();

        // Update the role using partial update
        Role partialUpdatedRole = new Role();
        partialUpdatedRole.setId(role.getId());

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedRole.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedRole))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Role in the database
        List<Role> roleList = roleRepository.findAll().collectList().block();
        assertThat(roleList).hasSize(databaseSizeBeforeUpdate);
        Role testRole = roleList.get(roleList.size() - 1);
        assertThat(testRole.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    void fullUpdateRoleWithPatch() throws Exception {
        // Initialize the database
        role.setId(UUID.randomUUID().toString());
        roleRepository.save(role).block();

        int databaseSizeBeforeUpdate = roleRepository.findAll().collectList().block().size();

        // Update the role using partial update
        Role partialUpdatedRole = new Role();
        partialUpdatedRole.setId(role.getId());

        partialUpdatedRole.description(UPDATED_DESCRIPTION);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedRole.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedRole))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Role in the database
        List<Role> roleList = roleRepository.findAll().collectList().block();
        assertThat(roleList).hasSize(databaseSizeBeforeUpdate);
        Role testRole = roleList.get(roleList.size() - 1);
        assertThat(testRole.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    void patchNonExistingRole() throws Exception {
        int databaseSizeBeforeUpdate = roleRepository.findAll().collectList().block().size();
        role.setId(UUID.randomUUID().toString());

        // Create the Role
        RoleDTO roleDTO = roleMapper.toDto(role);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, roleDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(roleDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Role in the database
        List<Role> roleList = roleRepository.findAll().collectList().block();
        assertThat(roleList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchRole() throws Exception {
        int databaseSizeBeforeUpdate = roleRepository.findAll().collectList().block().size();
        role.setId(UUID.randomUUID().toString());

        // Create the Role
        RoleDTO roleDTO = roleMapper.toDto(role);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(roleDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Role in the database
        List<Role> roleList = roleRepository.findAll().collectList().block();
        assertThat(roleList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamRole() throws Exception {
        int databaseSizeBeforeUpdate = roleRepository.findAll().collectList().block().size();
        role.setId(UUID.randomUUID().toString());

        // Create the Role
        RoleDTO roleDTO = roleMapper.toDto(role);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(roleDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Role in the database
        List<Role> roleList = roleRepository.findAll().collectList().block();
        assertThat(roleList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteRole() {
        // Initialize the database
        role.setId(UUID.randomUUID().toString());
        roleRepository.save(role).block();

        int databaseSizeBeforeDelete = roleRepository.findAll().collectList().block().size();

        // Delete the role
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, role.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Role> roleList = roleRepository.findAll().collectList().block();
        assertThat(roleList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
