package com.erestaurant.admin.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.erestaurant.admin.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CompositePermissionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CompositePermission.class);
        CompositePermission compositePermission1 = new CompositePermission();
        compositePermission1.setId("id1");
        CompositePermission compositePermission2 = new CompositePermission();
        compositePermission2.setId(compositePermission1.getId());
        assertThat(compositePermission1).isEqualTo(compositePermission2);
        compositePermission2.setId("id2");
        assertThat(compositePermission1).isNotEqualTo(compositePermission2);
        compositePermission1.setId(null);
        assertThat(compositePermission1).isNotEqualTo(compositePermission2);
    }
}
