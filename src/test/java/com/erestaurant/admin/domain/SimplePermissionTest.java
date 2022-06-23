package com.erestaurant.admin.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.erestaurant.admin.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SimplePermissionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(SimplePermission.class);
        SimplePermission simplePermission1 = new SimplePermission();
        simplePermission1.setId("id1");
        SimplePermission simplePermission2 = new SimplePermission();
        simplePermission2.setId(simplePermission1.getId());
        assertThat(simplePermission1).isEqualTo(simplePermission2);
        simplePermission2.setId("id2");
        assertThat(simplePermission1).isNotEqualTo(simplePermission2);
        simplePermission1.setId(null);
        assertThat(simplePermission1).isNotEqualTo(simplePermission2);
    }
}
