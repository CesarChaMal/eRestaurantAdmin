package com.erestaurant.admin.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.erestaurant.admin.IntegrationTest;
import com.erestaurant.admin.domain.Admin;
import com.erestaurant.admin.repository.AdminRepository;
import com.erestaurant.admin.repository.EntityManager;
import com.erestaurant.admin.service.dto.AdminDTO;
import com.erestaurant.admin.service.mapper.AdminMapper;
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
 * Integration tests for the {@link AdminResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class AdminResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final byte[] DEFAULT_IMAGE = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_IMAGE = TestUtil.createByteArray(1, "1");
    private static final String DEFAULT_IMAGE_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_IMAGE_CONTENT_TYPE = "image/png";

    private static final String DEFAULT_EMAIL = "AAAAAAAAAA";
    private static final String UPDATED_EMAIL = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/admins";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Admin admin;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Admin createEntity(EntityManager em) {
        Admin admin = new Admin()
            .name(DEFAULT_NAME)
            .description(DEFAULT_DESCRIPTION)
            .image(DEFAULT_IMAGE)
            .imageContentType(DEFAULT_IMAGE_CONTENT_TYPE)
            .email(DEFAULT_EMAIL);
        return admin;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Admin createUpdatedEntity(EntityManager em) {
        Admin admin = new Admin()
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .image(UPDATED_IMAGE)
            .imageContentType(UPDATED_IMAGE_CONTENT_TYPE)
            .email(UPDATED_EMAIL);
        return admin;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Admin.class).block();
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
        admin = createEntity(em);
    }

    @Test
    void createAdmin() throws Exception {
        int databaseSizeBeforeCreate = adminRepository.findAll().collectList().block().size();
        // Create the Admin
        AdminDTO adminDTO = adminMapper.toDto(admin);
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(adminDTO))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Admin in the database
        List<Admin> adminList = adminRepository.findAll().collectList().block();
        assertThat(adminList).hasSize(databaseSizeBeforeCreate + 1);
        Admin testAdmin = adminList.get(adminList.size() - 1);
        assertThat(testAdmin.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testAdmin.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testAdmin.getImage()).isEqualTo(DEFAULT_IMAGE);
        assertThat(testAdmin.getImageContentType()).isEqualTo(DEFAULT_IMAGE_CONTENT_TYPE);
        assertThat(testAdmin.getEmail()).isEqualTo(DEFAULT_EMAIL);
    }

    @Test
    void createAdminWithExistingId() throws Exception {
        // Create the Admin with an existing ID
        admin.setId("existing_id");
        AdminDTO adminDTO = adminMapper.toDto(admin);

        int databaseSizeBeforeCreate = adminRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(adminDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Admin in the database
        List<Admin> adminList = adminRepository.findAll().collectList().block();
        assertThat(adminList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = adminRepository.findAll().collectList().block().size();
        // set the field null
        admin.setName(null);

        // Create the Admin, which fails.
        AdminDTO adminDTO = adminMapper.toDto(admin);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(adminDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Admin> adminList = adminRepository.findAll().collectList().block();
        assertThat(adminList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllAdminsAsStream() {
        // Initialize the database
        admin.setId(UUID.randomUUID().toString());
        adminRepository.save(admin).block();

        List<Admin> adminList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(AdminDTO.class)
            .getResponseBody()
            .map(adminMapper::toEntity)
            .filter(admin::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(adminList).isNotNull();
        assertThat(adminList).hasSize(1);
        Admin testAdmin = adminList.get(0);
        assertThat(testAdmin.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testAdmin.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testAdmin.getImage()).isEqualTo(DEFAULT_IMAGE);
        assertThat(testAdmin.getImageContentType()).isEqualTo(DEFAULT_IMAGE_CONTENT_TYPE);
        assertThat(testAdmin.getEmail()).isEqualTo(DEFAULT_EMAIL);
    }

    @Test
    void getAllAdmins() {
        // Initialize the database
        admin.setId(UUID.randomUUID().toString());
        adminRepository.save(admin).block();

        // Get all the adminList
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
            .value(hasItem(admin.getId()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME))
            .jsonPath("$.[*].description")
            .value(hasItem(DEFAULT_DESCRIPTION.toString()))
            .jsonPath("$.[*].imageContentType")
            .value(hasItem(DEFAULT_IMAGE_CONTENT_TYPE))
            .jsonPath("$.[*].image")
            .value(hasItem(Base64Utils.encodeToString(DEFAULT_IMAGE)))
            .jsonPath("$.[*].email")
            .value(hasItem(DEFAULT_EMAIL));
    }

    @Test
    void getAdmin() {
        // Initialize the database
        admin.setId(UUID.randomUUID().toString());
        adminRepository.save(admin).block();

        // Get the admin
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, admin.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(admin.getId()))
            .jsonPath("$.name")
            .value(is(DEFAULT_NAME))
            .jsonPath("$.description")
            .value(is(DEFAULT_DESCRIPTION.toString()))
            .jsonPath("$.imageContentType")
            .value(is(DEFAULT_IMAGE_CONTENT_TYPE))
            .jsonPath("$.image")
            .value(is(Base64Utils.encodeToString(DEFAULT_IMAGE)))
            .jsonPath("$.email")
            .value(is(DEFAULT_EMAIL));
    }

    @Test
    void getNonExistingAdmin() {
        // Get the admin
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewAdmin() throws Exception {
        // Initialize the database
        admin.setId(UUID.randomUUID().toString());
        adminRepository.save(admin).block();

        int databaseSizeBeforeUpdate = adminRepository.findAll().collectList().block().size();

        // Update the admin
        Admin updatedAdmin = adminRepository.findById(admin.getId()).block();
        updatedAdmin
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .image(UPDATED_IMAGE)
            .imageContentType(UPDATED_IMAGE_CONTENT_TYPE)
            .email(UPDATED_EMAIL);
        AdminDTO adminDTO = adminMapper.toDto(updatedAdmin);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, adminDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(adminDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Admin in the database
        List<Admin> adminList = adminRepository.findAll().collectList().block();
        assertThat(adminList).hasSize(databaseSizeBeforeUpdate);
        Admin testAdmin = adminList.get(adminList.size() - 1);
        assertThat(testAdmin.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAdmin.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testAdmin.getImage()).isEqualTo(UPDATED_IMAGE);
        assertThat(testAdmin.getImageContentType()).isEqualTo(UPDATED_IMAGE_CONTENT_TYPE);
        assertThat(testAdmin.getEmail()).isEqualTo(UPDATED_EMAIL);
    }

    @Test
    void putNonExistingAdmin() throws Exception {
        int databaseSizeBeforeUpdate = adminRepository.findAll().collectList().block().size();
        admin.setId(UUID.randomUUID().toString());

        // Create the Admin
        AdminDTO adminDTO = adminMapper.toDto(admin);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, adminDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(adminDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Admin in the database
        List<Admin> adminList = adminRepository.findAll().collectList().block();
        assertThat(adminList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchAdmin() throws Exception {
        int databaseSizeBeforeUpdate = adminRepository.findAll().collectList().block().size();
        admin.setId(UUID.randomUUID().toString());

        // Create the Admin
        AdminDTO adminDTO = adminMapper.toDto(admin);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(adminDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Admin in the database
        List<Admin> adminList = adminRepository.findAll().collectList().block();
        assertThat(adminList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamAdmin() throws Exception {
        int databaseSizeBeforeUpdate = adminRepository.findAll().collectList().block().size();
        admin.setId(UUID.randomUUID().toString());

        // Create the Admin
        AdminDTO adminDTO = adminMapper.toDto(admin);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(adminDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Admin in the database
        List<Admin> adminList = adminRepository.findAll().collectList().block();
        assertThat(adminList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateAdminWithPatch() throws Exception {
        // Initialize the database
        admin.setId(UUID.randomUUID().toString());
        adminRepository.save(admin).block();

        int databaseSizeBeforeUpdate = adminRepository.findAll().collectList().block().size();

        // Update the admin using partial update
        Admin partialUpdatedAdmin = new Admin();
        partialUpdatedAdmin.setId(admin.getId());

        partialUpdatedAdmin.name(UPDATED_NAME).image(UPDATED_IMAGE).imageContentType(UPDATED_IMAGE_CONTENT_TYPE).email(UPDATED_EMAIL);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedAdmin.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedAdmin))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Admin in the database
        List<Admin> adminList = adminRepository.findAll().collectList().block();
        assertThat(adminList).hasSize(databaseSizeBeforeUpdate);
        Admin testAdmin = adminList.get(adminList.size() - 1);
        assertThat(testAdmin.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAdmin.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testAdmin.getImage()).isEqualTo(UPDATED_IMAGE);
        assertThat(testAdmin.getImageContentType()).isEqualTo(UPDATED_IMAGE_CONTENT_TYPE);
        assertThat(testAdmin.getEmail()).isEqualTo(UPDATED_EMAIL);
    }

    @Test
    void fullUpdateAdminWithPatch() throws Exception {
        // Initialize the database
        admin.setId(UUID.randomUUID().toString());
        adminRepository.save(admin).block();

        int databaseSizeBeforeUpdate = adminRepository.findAll().collectList().block().size();

        // Update the admin using partial update
        Admin partialUpdatedAdmin = new Admin();
        partialUpdatedAdmin.setId(admin.getId());

        partialUpdatedAdmin
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .image(UPDATED_IMAGE)
            .imageContentType(UPDATED_IMAGE_CONTENT_TYPE)
            .email(UPDATED_EMAIL);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedAdmin.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedAdmin))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Admin in the database
        List<Admin> adminList = adminRepository.findAll().collectList().block();
        assertThat(adminList).hasSize(databaseSizeBeforeUpdate);
        Admin testAdmin = adminList.get(adminList.size() - 1);
        assertThat(testAdmin.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAdmin.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testAdmin.getImage()).isEqualTo(UPDATED_IMAGE);
        assertThat(testAdmin.getImageContentType()).isEqualTo(UPDATED_IMAGE_CONTENT_TYPE);
        assertThat(testAdmin.getEmail()).isEqualTo(UPDATED_EMAIL);
    }

    @Test
    void patchNonExistingAdmin() throws Exception {
        int databaseSizeBeforeUpdate = adminRepository.findAll().collectList().block().size();
        admin.setId(UUID.randomUUID().toString());

        // Create the Admin
        AdminDTO adminDTO = adminMapper.toDto(admin);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, adminDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(adminDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Admin in the database
        List<Admin> adminList = adminRepository.findAll().collectList().block();
        assertThat(adminList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchAdmin() throws Exception {
        int databaseSizeBeforeUpdate = adminRepository.findAll().collectList().block().size();
        admin.setId(UUID.randomUUID().toString());

        // Create the Admin
        AdminDTO adminDTO = adminMapper.toDto(admin);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(adminDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Admin in the database
        List<Admin> adminList = adminRepository.findAll().collectList().block();
        assertThat(adminList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamAdmin() throws Exception {
        int databaseSizeBeforeUpdate = adminRepository.findAll().collectList().block().size();
        admin.setId(UUID.randomUUID().toString());

        // Create the Admin
        AdminDTO adminDTO = adminMapper.toDto(admin);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(adminDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Admin in the database
        List<Admin> adminList = adminRepository.findAll().collectList().block();
        assertThat(adminList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteAdmin() {
        // Initialize the database
        admin.setId(UUID.randomUUID().toString());
        adminRepository.save(admin).block();

        int databaseSizeBeforeDelete = adminRepository.findAll().collectList().block().size();

        // Delete the admin
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, admin.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Admin> adminList = adminRepository.findAll().collectList().block();
        assertThat(adminList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
