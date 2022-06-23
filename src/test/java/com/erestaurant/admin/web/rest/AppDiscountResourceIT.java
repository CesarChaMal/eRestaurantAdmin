package com.erestaurant.admin.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.erestaurant.admin.IntegrationTest;
import com.erestaurant.admin.domain.AppDiscount;
import com.erestaurant.admin.repository.AppDiscountRepository;
import com.erestaurant.admin.repository.EntityManager;
import com.erestaurant.admin.service.dto.AppDiscountDTO;
import com.erestaurant.admin.service.mapper.AppDiscountMapper;
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
 * Integration tests for the {@link AppDiscountResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class AppDiscountResourceIT {

    private static final String DEFAULT_CODE = "AAAAAAAAAA";
    private static final String UPDATED_CODE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final Float DEFAULT_PERCENTAGE = 1F;
    private static final Float UPDATED_PERCENTAGE = 2F;

    private static final String ENTITY_API_URL = "/api/app-discounts";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private AppDiscountRepository appDiscountRepository;

    @Autowired
    private AppDiscountMapper appDiscountMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private AppDiscount appDiscount;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AppDiscount createEntity(EntityManager em) {
        AppDiscount appDiscount = new AppDiscount().code(DEFAULT_CODE).description(DEFAULT_DESCRIPTION).percentage(DEFAULT_PERCENTAGE);
        return appDiscount;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AppDiscount createUpdatedEntity(EntityManager em) {
        AppDiscount appDiscount = new AppDiscount().code(UPDATED_CODE).description(UPDATED_DESCRIPTION).percentage(UPDATED_PERCENTAGE);
        return appDiscount;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(AppDiscount.class).block();
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
        appDiscount = createEntity(em);
    }

    @Test
    void createAppDiscount() throws Exception {
        int databaseSizeBeforeCreate = appDiscountRepository.findAll().collectList().block().size();
        // Create the AppDiscount
        AppDiscountDTO appDiscountDTO = appDiscountMapper.toDto(appDiscount);
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(appDiscountDTO))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the AppDiscount in the database
        List<AppDiscount> appDiscountList = appDiscountRepository.findAll().collectList().block();
        assertThat(appDiscountList).hasSize(databaseSizeBeforeCreate + 1);
        AppDiscount testAppDiscount = appDiscountList.get(appDiscountList.size() - 1);
        assertThat(testAppDiscount.getCode()).isEqualTo(DEFAULT_CODE);
        assertThat(testAppDiscount.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testAppDiscount.getPercentage()).isEqualTo(DEFAULT_PERCENTAGE);
    }

    @Test
    void createAppDiscountWithExistingId() throws Exception {
        // Create the AppDiscount with an existing ID
        appDiscount.setId("existing_id");
        AppDiscountDTO appDiscountDTO = appDiscountMapper.toDto(appDiscount);

        int databaseSizeBeforeCreate = appDiscountRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(appDiscountDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the AppDiscount in the database
        List<AppDiscount> appDiscountList = appDiscountRepository.findAll().collectList().block();
        assertThat(appDiscountList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void checkCodeIsRequired() throws Exception {
        int databaseSizeBeforeTest = appDiscountRepository.findAll().collectList().block().size();
        // set the field null
        appDiscount.setCode(null);

        // Create the AppDiscount, which fails.
        AppDiscountDTO appDiscountDTO = appDiscountMapper.toDto(appDiscount);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(appDiscountDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<AppDiscount> appDiscountList = appDiscountRepository.findAll().collectList().block();
        assertThat(appDiscountList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkPercentageIsRequired() throws Exception {
        int databaseSizeBeforeTest = appDiscountRepository.findAll().collectList().block().size();
        // set the field null
        appDiscount.setPercentage(null);

        // Create the AppDiscount, which fails.
        AppDiscountDTO appDiscountDTO = appDiscountMapper.toDto(appDiscount);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(appDiscountDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<AppDiscount> appDiscountList = appDiscountRepository.findAll().collectList().block();
        assertThat(appDiscountList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllAppDiscountsAsStream() {
        // Initialize the database
        appDiscount.setId(UUID.randomUUID().toString());
        appDiscountRepository.save(appDiscount).block();

        List<AppDiscount> appDiscountList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(AppDiscountDTO.class)
            .getResponseBody()
            .map(appDiscountMapper::toEntity)
            .filter(appDiscount::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(appDiscountList).isNotNull();
        assertThat(appDiscountList).hasSize(1);
        AppDiscount testAppDiscount = appDiscountList.get(0);
        assertThat(testAppDiscount.getCode()).isEqualTo(DEFAULT_CODE);
        assertThat(testAppDiscount.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testAppDiscount.getPercentage()).isEqualTo(DEFAULT_PERCENTAGE);
    }

    @Test
    void getAllAppDiscounts() {
        // Initialize the database
        appDiscount.setId(UUID.randomUUID().toString());
        appDiscountRepository.save(appDiscount).block();

        // Get all the appDiscountList
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
            .value(hasItem(appDiscount.getId()))
            .jsonPath("$.[*].code")
            .value(hasItem(DEFAULT_CODE))
            .jsonPath("$.[*].description")
            .value(hasItem(DEFAULT_DESCRIPTION.toString()))
            .jsonPath("$.[*].percentage")
            .value(hasItem(DEFAULT_PERCENTAGE.doubleValue()));
    }

    @Test
    void getAppDiscount() {
        // Initialize the database
        appDiscount.setId(UUID.randomUUID().toString());
        appDiscountRepository.save(appDiscount).block();

        // Get the appDiscount
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, appDiscount.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(appDiscount.getId()))
            .jsonPath("$.code")
            .value(is(DEFAULT_CODE))
            .jsonPath("$.description")
            .value(is(DEFAULT_DESCRIPTION.toString()))
            .jsonPath("$.percentage")
            .value(is(DEFAULT_PERCENTAGE.doubleValue()));
    }

    @Test
    void getNonExistingAppDiscount() {
        // Get the appDiscount
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewAppDiscount() throws Exception {
        // Initialize the database
        appDiscount.setId(UUID.randomUUID().toString());
        appDiscountRepository.save(appDiscount).block();

        int databaseSizeBeforeUpdate = appDiscountRepository.findAll().collectList().block().size();

        // Update the appDiscount
        AppDiscount updatedAppDiscount = appDiscountRepository.findById(appDiscount.getId()).block();
        updatedAppDiscount.code(UPDATED_CODE).description(UPDATED_DESCRIPTION).percentage(UPDATED_PERCENTAGE);
        AppDiscountDTO appDiscountDTO = appDiscountMapper.toDto(updatedAppDiscount);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, appDiscountDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(appDiscountDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the AppDiscount in the database
        List<AppDiscount> appDiscountList = appDiscountRepository.findAll().collectList().block();
        assertThat(appDiscountList).hasSize(databaseSizeBeforeUpdate);
        AppDiscount testAppDiscount = appDiscountList.get(appDiscountList.size() - 1);
        assertThat(testAppDiscount.getCode()).isEqualTo(UPDATED_CODE);
        assertThat(testAppDiscount.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testAppDiscount.getPercentage()).isEqualTo(UPDATED_PERCENTAGE);
    }

    @Test
    void putNonExistingAppDiscount() throws Exception {
        int databaseSizeBeforeUpdate = appDiscountRepository.findAll().collectList().block().size();
        appDiscount.setId(UUID.randomUUID().toString());

        // Create the AppDiscount
        AppDiscountDTO appDiscountDTO = appDiscountMapper.toDto(appDiscount);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, appDiscountDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(appDiscountDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the AppDiscount in the database
        List<AppDiscount> appDiscountList = appDiscountRepository.findAll().collectList().block();
        assertThat(appDiscountList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchAppDiscount() throws Exception {
        int databaseSizeBeforeUpdate = appDiscountRepository.findAll().collectList().block().size();
        appDiscount.setId(UUID.randomUUID().toString());

        // Create the AppDiscount
        AppDiscountDTO appDiscountDTO = appDiscountMapper.toDto(appDiscount);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(appDiscountDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the AppDiscount in the database
        List<AppDiscount> appDiscountList = appDiscountRepository.findAll().collectList().block();
        assertThat(appDiscountList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamAppDiscount() throws Exception {
        int databaseSizeBeforeUpdate = appDiscountRepository.findAll().collectList().block().size();
        appDiscount.setId(UUID.randomUUID().toString());

        // Create the AppDiscount
        AppDiscountDTO appDiscountDTO = appDiscountMapper.toDto(appDiscount);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(appDiscountDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the AppDiscount in the database
        List<AppDiscount> appDiscountList = appDiscountRepository.findAll().collectList().block();
        assertThat(appDiscountList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateAppDiscountWithPatch() throws Exception {
        // Initialize the database
        appDiscount.setId(UUID.randomUUID().toString());
        appDiscountRepository.save(appDiscount).block();

        int databaseSizeBeforeUpdate = appDiscountRepository.findAll().collectList().block().size();

        // Update the appDiscount using partial update
        AppDiscount partialUpdatedAppDiscount = new AppDiscount();
        partialUpdatedAppDiscount.setId(appDiscount.getId());

        partialUpdatedAppDiscount.percentage(UPDATED_PERCENTAGE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedAppDiscount.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedAppDiscount))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the AppDiscount in the database
        List<AppDiscount> appDiscountList = appDiscountRepository.findAll().collectList().block();
        assertThat(appDiscountList).hasSize(databaseSizeBeforeUpdate);
        AppDiscount testAppDiscount = appDiscountList.get(appDiscountList.size() - 1);
        assertThat(testAppDiscount.getCode()).isEqualTo(DEFAULT_CODE);
        assertThat(testAppDiscount.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testAppDiscount.getPercentage()).isEqualTo(UPDATED_PERCENTAGE);
    }

    @Test
    void fullUpdateAppDiscountWithPatch() throws Exception {
        // Initialize the database
        appDiscount.setId(UUID.randomUUID().toString());
        appDiscountRepository.save(appDiscount).block();

        int databaseSizeBeforeUpdate = appDiscountRepository.findAll().collectList().block().size();

        // Update the appDiscount using partial update
        AppDiscount partialUpdatedAppDiscount = new AppDiscount();
        partialUpdatedAppDiscount.setId(appDiscount.getId());

        partialUpdatedAppDiscount.code(UPDATED_CODE).description(UPDATED_DESCRIPTION).percentage(UPDATED_PERCENTAGE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedAppDiscount.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedAppDiscount))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the AppDiscount in the database
        List<AppDiscount> appDiscountList = appDiscountRepository.findAll().collectList().block();
        assertThat(appDiscountList).hasSize(databaseSizeBeforeUpdate);
        AppDiscount testAppDiscount = appDiscountList.get(appDiscountList.size() - 1);
        assertThat(testAppDiscount.getCode()).isEqualTo(UPDATED_CODE);
        assertThat(testAppDiscount.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testAppDiscount.getPercentage()).isEqualTo(UPDATED_PERCENTAGE);
    }

    @Test
    void patchNonExistingAppDiscount() throws Exception {
        int databaseSizeBeforeUpdate = appDiscountRepository.findAll().collectList().block().size();
        appDiscount.setId(UUID.randomUUID().toString());

        // Create the AppDiscount
        AppDiscountDTO appDiscountDTO = appDiscountMapper.toDto(appDiscount);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, appDiscountDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(appDiscountDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the AppDiscount in the database
        List<AppDiscount> appDiscountList = appDiscountRepository.findAll().collectList().block();
        assertThat(appDiscountList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchAppDiscount() throws Exception {
        int databaseSizeBeforeUpdate = appDiscountRepository.findAll().collectList().block().size();
        appDiscount.setId(UUID.randomUUID().toString());

        // Create the AppDiscount
        AppDiscountDTO appDiscountDTO = appDiscountMapper.toDto(appDiscount);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(appDiscountDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the AppDiscount in the database
        List<AppDiscount> appDiscountList = appDiscountRepository.findAll().collectList().block();
        assertThat(appDiscountList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamAppDiscount() throws Exception {
        int databaseSizeBeforeUpdate = appDiscountRepository.findAll().collectList().block().size();
        appDiscount.setId(UUID.randomUUID().toString());

        // Create the AppDiscount
        AppDiscountDTO appDiscountDTO = appDiscountMapper.toDto(appDiscount);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(appDiscountDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the AppDiscount in the database
        List<AppDiscount> appDiscountList = appDiscountRepository.findAll().collectList().block();
        assertThat(appDiscountList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteAppDiscount() {
        // Initialize the database
        appDiscount.setId(UUID.randomUUID().toString());
        appDiscountRepository.save(appDiscount).block();

        int databaseSizeBeforeDelete = appDiscountRepository.findAll().collectList().block().size();

        // Delete the appDiscount
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, appDiscount.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<AppDiscount> appDiscountList = appDiscountRepository.findAll().collectList().block();
        assertThat(appDiscountList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
