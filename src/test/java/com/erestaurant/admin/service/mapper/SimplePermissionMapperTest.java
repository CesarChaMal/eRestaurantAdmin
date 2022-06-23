package com.erestaurant.admin.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SimplePermissionMapperTest {

    private SimplePermissionMapper simplePermissionMapper;

    @BeforeEach
    public void setUp() {
        simplePermissionMapper = new SimplePermissionMapperImpl();
    }
}
