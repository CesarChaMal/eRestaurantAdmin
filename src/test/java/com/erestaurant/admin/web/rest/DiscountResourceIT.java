package com.erestaurant.admin.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.erestaurant.admin.IntegrationTest;
import com.erestaurant.admin.domain.Discount;
import com.erestaurant.admin.repository.DiscountRepository;
import com.erestaurant.admin.repository.EntityManager;
import com.erestaurant.admin.service.dto.DiscountDTO;
import com.erestaurant.admin.service.mapper.DiscountMapper;
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
 * Integration tests for the {@link DiscountResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class DiscountResourceIT {

    private static final String DEFAULT_CODE = "AAAAAAAAAA";
    private static final String UPDATED_CODE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final Float DEFAULT_PERCENTAGE = 1F;
    private static final Float UPDATED_PERCENTAGE = 2F;

    private static final String ENTITY_API_URL = "/api/discounts";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private DiscountRepository discountRepository;

    @Autowired
    private DiscountMapper discountMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Discount discount;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Discount createEntity(EntityManager em) {
        Discount discount = new Discount().code(DEFAULT_CODE).description(DEFAULT_DESCRIPTION).percentage(DEFAULT_PERCENTAGE);
        return discount;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Discount createUpdatedEntity(EntityManager em) {
        Discount discount = new Discount().code(UPDATED_CODE).description(UPDATED_DESCRIPTION).percentage(UPDATED_PERCENTAGE);
        return discount;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Discount.class).block();
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
        discount = createEntity(em);
    }

    @Test
    void createDiscount() throws Exception {
        int databaseSizeBeforeCreate = discountRepository.findAll().collectList().block().size();
        // Create the Discount
        DiscountDTO discountDTO = discountMapper.toDto(discount);
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(discountDTO))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Discount in the database
        List<Discount> discountList = discountRepository.findAll().collectList().block();
        assertThat(discountList).hasSize(databaseSizeBeforeCreate + 1);
        Discount testDiscount = discountList.get(discountList.size() - 1);
        assertThat(testDiscount.getCode()).isEqualTo(DEFAULT_CODE);
        assertThat(testDiscount.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testDiscount.getPercentage()).isEqualTo(DEFAULT_PERCENTAGE);
    }

    @Test
    void createDiscountWithExistingId() throws Exception {
        // Create the Discount with an existing ID
        discount.setId("existing_id");
        DiscountDTO discountDTO = discountMapper.toDto(discount);

        int databaseSizeBeforeCreate = discountRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(discountDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Discount in the database
        List<Discount> discountList = discountRepository.findAll().collectList().block();
        assertThat(discountList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void checkCodeIsRequired() throws Exception {
        int databaseSizeBeforeTest = discountRepository.findAll().collectList().block().size();
        // set the field null
        discount.setCode(null);

        // Create the Discount, which fails.
        DiscountDTO discountDTO = discountMapper.toDto(discount);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(discountDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Discount> discountList = discountRepository.findAll().collectList().block();
        assertThat(discountList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkPercentageIsRequired() throws Exception {
        int databaseSizeBeforeTest = discountRepository.findAll().collectList().block().size();
        // set the field null
        discount.setPercentage(null);

        // Create the Discount, which fails.
        DiscountDTO discountDTO = discountMapper.toDto(discount);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(discountDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Discount> discountList = discountRepository.findAll().collectList().block();
        assertThat(discountList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllDiscountsAsStream() {
        // Initialize the database
        discount.setId(UUID.randomUUID().toString());
        discountRepository.save(discount).block();

        List<Discount> discountList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(DiscountDTO.class)
            .getResponseBody()
            .map(discountMapper::toEntity)
            .filter(discount::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(discountList).isNotNull();
        assertThat(discountList).hasSize(1);
        Discount testDiscount = discountList.get(0);
        assertThat(testDiscount.getCode()).isEqualTo(DEFAULT_CODE);
        assertThat(testDiscount.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testDiscount.getPercentage()).isEqualTo(DEFAULT_PERCENTAGE);
    }

    @Test
    void getAllDiscounts() {
        // Initialize the database
        discount.setId(UUID.randomUUID().toString());
        discountRepository.save(discount).block();

        // Get all the discountList
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
            .value(hasItem(discount.getId()))
            .jsonPath("$.[*].code")
            .value(hasItem(DEFAULT_CODE))
            .jsonPath("$.[*].description")
            .value(hasItem(DEFAULT_DESCRIPTION.toString()))
            .jsonPath("$.[*].percentage")
            .value(hasItem(DEFAULT_PERCENTAGE.doubleValue()));
    }

    @Test
    void getDiscount() {
        // Initialize the database
        discount.setId(UUID.randomUUID().toString());
        discountRepository.save(discount).block();

        // Get the discount
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, discount.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(discount.getId()))
            .jsonPath("$.code")
            .value(is(DEFAULT_CODE))
            .jsonPath("$.description")
            .value(is(DEFAULT_DESCRIPTION.toString()))
            .jsonPath("$.percentage")
            .value(is(DEFAULT_PERCENTAGE.doubleValue()));
    }

    @Test
    void getNonExistingDiscount() {
        // Get the discount
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewDiscount() throws Exception {
        // Initialize the database
        discount.setId(UUID.randomUUID().toString());
        discountRepository.save(discount).block();

        int databaseSizeBeforeUpdate = discountRepository.findAll().collectList().block().size();

        // Update the discount
        Discount updatedDiscount = discountRepository.findById(discount.getId()).block();
        updatedDiscount.code(UPDATED_CODE).description(UPDATED_DESCRIPTION).percentage(UPDATED_PERCENTAGE);
        DiscountDTO discountDTO = discountMapper.toDto(updatedDiscount);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, discountDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(discountDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Discount in the database
        List<Discount> discountList = discountRepository.findAll().collectList().block();
        assertThat(discountList).hasSize(databaseSizeBeforeUpdate);
        Discount testDiscount = discountList.get(discountList.size() - 1);
        assertThat(testDiscount.getCode()).isEqualTo(UPDATED_CODE);
        assertThat(testDiscount.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testDiscount.getPercentage()).isEqualTo(UPDATED_PERCENTAGE);
    }

    @Test
    void putNonExistingDiscount() throws Exception {
        int databaseSizeBeforeUpdate = discountRepository.findAll().collectList().block().size();
        discount.setId(UUID.randomUUID().toString());

        // Create the Discount
        DiscountDTO discountDTO = discountMapper.toDto(discount);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, discountDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(discountDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Discount in the database
        List<Discount> discountList = discountRepository.findAll().collectList().block();
        assertThat(discountList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchDiscount() throws Exception {
        int databaseSizeBeforeUpdate = discountRepository.findAll().collectList().block().size();
        discount.setId(UUID.randomUUID().toString());

        // Create the Discount
        DiscountDTO discountDTO = discountMapper.toDto(discount);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(discountDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Discount in the database
        List<Discount> discountList = discountRepository.findAll().collectList().block();
        assertThat(discountList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamDiscount() throws Exception {
        int databaseSizeBeforeUpdate = discountRepository.findAll().collectList().block().size();
        discount.setId(UUID.randomUUID().toString());

        // Create the Discount
        DiscountDTO discountDTO = discountMapper.toDto(discount);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(discountDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Discount in the database
        List<Discount> discountList = discountRepository.findAll().collectList().block();
        assertThat(discountList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateDiscountWithPatch() throws Exception {
        // Initialize the database
        discount.setId(UUID.randomUUID().toString());
        discountRepository.save(discount).block();

        int databaseSizeBeforeUpdate = discountRepository.findAll().collectList().block().size();

        // Update the discount using partial update
        Discount partialUpdatedDiscount = new Discount();
        partialUpdatedDiscount.setId(discount.getId());

        partialUpdatedDiscount.code(UPDATED_CODE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedDiscount.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedDiscount))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Discount in the database
        List<Discount> discountList = discountRepository.findAll().collectList().block();
        assertThat(discountList).hasSize(databaseSizeBeforeUpdate);
        Discount testDiscount = discountList.get(discountList.size() - 1);
        assertThat(testDiscount.getCode()).isEqualTo(UPDATED_CODE);
        assertThat(testDiscount.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testDiscount.getPercentage()).isEqualTo(DEFAULT_PERCENTAGE);
    }

    @Test
    void fullUpdateDiscountWithPatch() throws Exception {
        // Initialize the database
        discount.setId(UUID.randomUUID().toString());
        discountRepository.save(discount).block();

        int databaseSizeBeforeUpdate = discountRepository.findAll().collectList().block().size();

        // Update the discount using partial update
        Discount partialUpdatedDiscount = new Discount();
        partialUpdatedDiscount.setId(discount.getId());

        partialUpdatedDiscount.code(UPDATED_CODE).description(UPDATED_DESCRIPTION).percentage(UPDATED_PERCENTAGE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedDiscount.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedDiscount))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Discount in the database
        List<Discount> discountList = discountRepository.findAll().collectList().block();
        assertThat(discountList).hasSize(databaseSizeBeforeUpdate);
        Discount testDiscount = discountList.get(discountList.size() - 1);
        assertThat(testDiscount.getCode()).isEqualTo(UPDATED_CODE);
        assertThat(testDiscount.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testDiscount.getPercentage()).isEqualTo(UPDATED_PERCENTAGE);
    }

    @Test
    void patchNonExistingDiscount() throws Exception {
        int databaseSizeBeforeUpdate = discountRepository.findAll().collectList().block().size();
        discount.setId(UUID.randomUUID().toString());

        // Create the Discount
        DiscountDTO discountDTO = discountMapper.toDto(discount);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, discountDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(discountDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Discount in the database
        List<Discount> discountList = discountRepository.findAll().collectList().block();
        assertThat(discountList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchDiscount() throws Exception {
        int databaseSizeBeforeUpdate = discountRepository.findAll().collectList().block().size();
        discount.setId(UUID.randomUUID().toString());

        // Create the Discount
        DiscountDTO discountDTO = discountMapper.toDto(discount);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(discountDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Discount in the database
        List<Discount> discountList = discountRepository.findAll().collectList().block();
        assertThat(discountList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamDiscount() throws Exception {
        int databaseSizeBeforeUpdate = discountRepository.findAll().collectList().block().size();
        discount.setId(UUID.randomUUID().toString());

        // Create the Discount
        DiscountDTO discountDTO = discountMapper.toDto(discount);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(discountDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Discount in the database
        List<Discount> discountList = discountRepository.findAll().collectList().block();
        assertThat(discountList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteDiscount() {
        // Initialize the database
        discount.setId(UUID.randomUUID().toString());
        discountRepository.save(discount).block();

        int databaseSizeBeforeDelete = discountRepository.findAll().collectList().block().size();

        // Delete the discount
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, discount.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Discount> discountList = discountRepository.findAll().collectList().block();
        assertThat(discountList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
