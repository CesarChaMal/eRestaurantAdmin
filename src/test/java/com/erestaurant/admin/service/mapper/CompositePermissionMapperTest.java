package com.erestaurant.admin.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CompositePermissionMapperTest {

    private CompositePermissionMapper compositePermissionMapper;

    @BeforeEach
    public void setUp() {
        compositePermissionMapper = new CompositePermissionMapperImpl();
    }
}
