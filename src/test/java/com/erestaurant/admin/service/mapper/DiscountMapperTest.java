package com.erestaurant.admin.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DiscountMapperTest {

    private DiscountMapper discountMapper;

    @BeforeEach
    public void setUp() {
        discountMapper = new DiscountMapperImpl();
    }
}
