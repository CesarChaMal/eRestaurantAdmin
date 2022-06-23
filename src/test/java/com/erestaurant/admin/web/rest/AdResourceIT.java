package com.erestaurant.admin.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.erestaurant.admin.IntegrationTest;
import com.erestaurant.admin.domain.Ad;
import com.erestaurant.admin.repository.AdRepository;
import com.erestaurant.admin.repository.EntityManager;
import com.erestaurant.admin.service.dto.AdDTO;
import com.erestaurant.admin.service.mapper.AdMapper;
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
 * Integration tests for the {@link AdResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class AdResourceIT {

    private static final String DEFAULT_URL = "AAAAAAAAAA";
    private static final String UPDATED_URL = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/ads";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private AdRepository adRepository;

    @Autowired
    private AdMapper adMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Ad ad;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Ad createEntity(EntityManager em) {
        Ad ad = new Ad().url(DEFAULT_URL).description(DEFAULT_DESCRIPTION);
        return ad;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Ad createUpdatedEntity(EntityManager em) {
        Ad ad = new Ad().url(UPDATED_URL).description(UPDATED_DESCRIPTION);
        return ad;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Ad.class).block();
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
        ad = createEntity(em);
    }

    @Test
    void createAd() throws Exception {
        int databaseSizeBeforeCreate = adRepository.findAll().collectList().block().size();
        // Create the Ad
        AdDTO adDTO = adMapper.toDto(ad);
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(adDTO))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Ad in the database
        List<Ad> adList = adRepository.findAll().collectList().block();
        assertThat(adList).hasSize(databaseSizeBeforeCreate + 1);
        Ad testAd = adList.get(adList.size() - 1);
        assertThat(testAd.getUrl()).isEqualTo(DEFAULT_URL);
        assertThat(testAd.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    void createAdWithExistingId() throws Exception {
        // Create the Ad with an existing ID
        ad.setId("existing_id");
        AdDTO adDTO = adMapper.toDto(ad);

        int databaseSizeBeforeCreate = adRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(adDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Ad in the database
        List<Ad> adList = adRepository.findAll().collectList().block();
        assertThat(adList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void checkUrlIsRequired() throws Exception {
        int databaseSizeBeforeTest = adRepository.findAll().collectList().block().size();
        // set the field null
        ad.setUrl(null);

        // Create the Ad, which fails.
        AdDTO adDTO = adMapper.toDto(ad);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(adDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Ad> adList = adRepository.findAll().collectList().block();
        assertThat(adList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllAdsAsStream() {
        // Initialize the database
        ad.setId(UUID.randomUUID().toString());
        adRepository.save(ad).block();

        List<Ad> adList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(AdDTO.class)
            .getResponseBody()
            .map(adMapper::toEntity)
            .filter(ad::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(adList).isNotNull();
        assertThat(adList).hasSize(1);
        Ad testAd = adList.get(0);
        assertThat(testAd.getUrl()).isEqualTo(DEFAULT_URL);
        assertThat(testAd.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    void getAllAds() {
        // Initialize the database
        ad.setId(UUID.randomUUID().toString());
        adRepository.save(ad).block();

        // Get all the adList
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
            .value(hasItem(ad.getId()))
            .jsonPath("$.[*].url")
            .value(hasItem(DEFAULT_URL))
            .jsonPath("$.[*].description")
            .value(hasItem(DEFAULT_DESCRIPTION.toString()));
    }

    @Test
    void getAd() {
        // Initialize the database
        ad.setId(UUID.randomUUID().toString());
        adRepository.save(ad).block();

        // Get the ad
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, ad.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(ad.getId()))
            .jsonPath("$.url")
            .value(is(DEFAULT_URL))
            .jsonPath("$.description")
            .value(is(DEFAULT_DESCRIPTION.toString()));
    }

    @Test
    void getNonExistingAd() {
        // Get the ad
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewAd() throws Exception {
        // Initialize the database
        ad.setId(UUID.randomUUID().toString());
        adRepository.save(ad).block();

        int databaseSizeBeforeUpdate = adRepository.findAll().collectList().block().size();

        // Update the ad
        Ad updatedAd = adRepository.findById(ad.getId()).block();
        updatedAd.url(UPDATED_URL).description(UPDATED_DESCRIPTION);
        AdDTO adDTO = adMapper.toDto(updatedAd);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, adDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(adDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Ad in the database
        List<Ad> adList = adRepository.findAll().collectList().block();
        assertThat(adList).hasSize(databaseSizeBeforeUpdate);
        Ad testAd = adList.get(adList.size() - 1);
        assertThat(testAd.getUrl()).isEqualTo(UPDATED_URL);
        assertThat(testAd.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    void putNonExistingAd() throws Exception {
        int databaseSizeBeforeUpdate = adRepository.findAll().collectList().block().size();
        ad.setId(UUID.randomUUID().toString());

        // Create the Ad
        AdDTO adDTO = adMapper.toDto(ad);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, adDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(adDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Ad in the database
        List<Ad> adList = adRepository.findAll().collectList().block();
        assertThat(adList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchAd() throws Exception {
        int databaseSizeBeforeUpdate = adRepository.findAll().collectList().block().size();
        ad.setId(UUID.randomUUID().toString());

        // Create the Ad
        AdDTO adDTO = adMapper.toDto(ad);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(adDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Ad in the database
        List<Ad> adList = adRepository.findAll().collectList().block();
        assertThat(adList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamAd() throws Exception {
        int databaseSizeBeforeUpdate = adRepository.findAll().collectList().block().size();
        ad.setId(UUID.randomUUID().toString());

        // Create the Ad
        AdDTO adDTO = adMapper.toDto(ad);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(adDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Ad in the database
        List<Ad> adList = adRepository.findAll().collectList().block();
        assertThat(adList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateAdWithPatch() throws Exception {
        // Initialize the database
        ad.setId(UUID.randomUUID().toString());
        adRepository.save(ad).block();

        int databaseSizeBeforeUpdate = adRepository.findAll().collectList().block().size();

        // Update the ad using partial update
        Ad partialUpdatedAd = new Ad();
        partialUpdatedAd.setId(ad.getId());

        partialUpdatedAd.url(UPDATED_URL).description(UPDATED_DESCRIPTION);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedAd.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedAd))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Ad in the database
        List<Ad> adList = adRepository.findAll().collectList().block();
        assertThat(adList).hasSize(databaseSizeBeforeUpdate);
        Ad testAd = adList.get(adList.size() - 1);
        assertThat(testAd.getUrl()).isEqualTo(UPDATED_URL);
        assertThat(testAd.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    void fullUpdateAdWithPatch() throws Exception {
        // Initialize the database
        ad.setId(UUID.randomUUID().toString());
        adRepository.save(ad).block();

        int databaseSizeBeforeUpdate = adRepository.findAll().collectList().block().size();

        // Update the ad using partial update
        Ad partialUpdatedAd = new Ad();
        partialUpdatedAd.setId(ad.getId());

        partialUpdatedAd.url(UPDATED_URL).description(UPDATED_DESCRIPTION);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedAd.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedAd))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Ad in the database
        List<Ad> adList = adRepository.findAll().collectList().block();
        assertThat(adList).hasSize(databaseSizeBeforeUpdate);
        Ad testAd = adList.get(adList.size() - 1);
        assertThat(testAd.getUrl()).isEqualTo(UPDATED_URL);
        assertThat(testAd.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    void patchNonExistingAd() throws Exception {
        int databaseSizeBeforeUpdate = adRepository.findAll().collectList().block().size();
        ad.setId(UUID.randomUUID().toString());

        // Create the Ad
        AdDTO adDTO = adMapper.toDto(ad);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, adDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(adDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Ad in the database
        List<Ad> adList = adRepository.findAll().collectList().block();
        assertThat(adList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchAd() throws Exception {
        int databaseSizeBeforeUpdate = adRepository.findAll().collectList().block().size();
        ad.setId(UUID.randomUUID().toString());

        // Create the Ad
        AdDTO adDTO = adMapper.toDto(ad);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(adDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Ad in the database
        List<Ad> adList = adRepository.findAll().collectList().block();
        assertThat(adList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamAd() throws Exception {
        int databaseSizeBeforeUpdate = adRepository.findAll().collectList().block().size();
        ad.setId(UUID.randomUUID().toString());

        // Create the Ad
        AdDTO adDTO = adMapper.toDto(ad);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(adDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Ad in the database
        List<Ad> adList = adRepository.findAll().collectList().block();
        assertThat(adList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteAd() {
        // Initialize the database
        ad.setId(UUID.randomUUID().toString());
        adRepository.save(ad).block();

        int databaseSizeBeforeDelete = adRepository.findAll().collectList().block().size();

        // Delete the ad
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, ad.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Ad> adList = adRepository.findAll().collectList().block();
        assertThat(adList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
