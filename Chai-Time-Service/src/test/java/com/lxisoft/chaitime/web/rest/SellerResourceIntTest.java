package com.lxisoft.chaitime.web.rest;

import com.lxisoft.chaitime.ChaiTimeApp;

import com.lxisoft.chaitime.domain.Seller;
import com.lxisoft.chaitime.repository.SellerRepository;
import com.lxisoft.chaitime.web.rest.errors.ExceptionTranslator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.util.List;


import static com.lxisoft.chaitime.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the SellerResource REST controller.
 *
 * @see SellerResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ChaiTimeApp.class)
public class SellerResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    private MockMvc restSellerMockMvc;

    private Seller seller;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final SellerResource sellerResource = new SellerResource(sellerRepository);
        this.restSellerMockMvc = MockMvcBuilders.standaloneSetup(sellerResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Seller createEntity(EntityManager em) {
        Seller seller = new Seller()
            .name(DEFAULT_NAME);
        return seller;
    }

    @Before
    public void initTest() {
        seller = createEntity(em);
    }

    @Test
    @Transactional
    public void createSeller() throws Exception {
        int databaseSizeBeforeCreate = sellerRepository.findAll().size();

        // Create the Seller
        restSellerMockMvc.perform(post("/api/sellers")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(seller)))
            .andExpect(status().isCreated());

        // Validate the Seller in the database
        List<Seller> sellerList = sellerRepository.findAll();
        assertThat(sellerList).hasSize(databaseSizeBeforeCreate + 1);
        Seller testSeller = sellerList.get(sellerList.size() - 1);
        assertThat(testSeller.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    @Transactional
    public void createSellerWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = sellerRepository.findAll().size();

        // Create the Seller with an existing ID
        seller.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restSellerMockMvc.perform(post("/api/sellers")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(seller)))
            .andExpect(status().isBadRequest());

        // Validate the Seller in the database
        List<Seller> sellerList = sellerRepository.findAll();
        assertThat(sellerList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllSellers() throws Exception {
        // Initialize the database
        sellerRepository.saveAndFlush(seller);

        // Get all the sellerList
        restSellerMockMvc.perform(get("/api/sellers?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(seller.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())));
    }
    
    @Test
    @Transactional
    public void getSeller() throws Exception {
        // Initialize the database
        sellerRepository.saveAndFlush(seller);

        // Get the seller
        restSellerMockMvc.perform(get("/api/sellers/{id}", seller.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(seller.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingSeller() throws Exception {
        // Get the seller
        restSellerMockMvc.perform(get("/api/sellers/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateSeller() throws Exception {
        // Initialize the database
        sellerRepository.saveAndFlush(seller);

        int databaseSizeBeforeUpdate = sellerRepository.findAll().size();

        // Update the seller
        Seller updatedSeller = sellerRepository.findById(seller.getId()).get();
        // Disconnect from session so that the updates on updatedSeller are not directly saved in db
        em.detach(updatedSeller);
        updatedSeller
            .name(UPDATED_NAME);

        restSellerMockMvc.perform(put("/api/sellers")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedSeller)))
            .andExpect(status().isOk());

        // Validate the Seller in the database
        List<Seller> sellerList = sellerRepository.findAll();
        assertThat(sellerList).hasSize(databaseSizeBeforeUpdate);
        Seller testSeller = sellerList.get(sellerList.size() - 1);
        assertThat(testSeller.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    @Transactional
    public void updateNonExistingSeller() throws Exception {
        int databaseSizeBeforeUpdate = sellerRepository.findAll().size();

        // Create the Seller

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSellerMockMvc.perform(put("/api/sellers")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(seller)))
            .andExpect(status().isBadRequest());

        // Validate the Seller in the database
        List<Seller> sellerList = sellerRepository.findAll();
        assertThat(sellerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteSeller() throws Exception {
        // Initialize the database
        sellerRepository.saveAndFlush(seller);

        int databaseSizeBeforeDelete = sellerRepository.findAll().size();

        // Delete the seller
        restSellerMockMvc.perform(delete("/api/sellers/{id}", seller.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Seller> sellerList = sellerRepository.findAll();
        assertThat(sellerList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Seller.class);
        Seller seller1 = new Seller();
        seller1.setId(1L);
        Seller seller2 = new Seller();
        seller2.setId(seller1.getId());
        assertThat(seller1).isEqualTo(seller2);
        seller2.setId(2L);
        assertThat(seller1).isNotEqualTo(seller2);
        seller1.setId(null);
        assertThat(seller1).isNotEqualTo(seller2);
    }
}
