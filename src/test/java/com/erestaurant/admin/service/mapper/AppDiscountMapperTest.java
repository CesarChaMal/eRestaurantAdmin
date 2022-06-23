package com.erestaurant.admin.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AppDiscountMapperTest {

    private AppDiscountMapper appDiscountMapper;

    @BeforeEach
    public void setUp() {
        appDiscountMapper = new AppDiscountMapperImpl();
    }
}
