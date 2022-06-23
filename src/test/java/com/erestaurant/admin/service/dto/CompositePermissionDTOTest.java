package com.erestaurant.admin.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.erestaurant.admin.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CompositePermissionDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(CompositePermissionDTO.class);
        CompositePermissionDTO compositePermissionDTO1 = new CompositePermissionDTO();
        compositePermissionDTO1.setId("id1");
        CompositePermissionDTO compositePermissionDTO2 = new CompositePermissionDTO();
        assertThat(compositePermissionDTO1).isNotEqualTo(compositePermissionDTO2);
        compositePermissionDTO2.setId(compositePermissionDTO1.getId());
        assertThat(compositePermissionDTO1).isEqualTo(compositePermissionDTO2);
        compositePermissionDTO2.setId("id2");
        assertThat(compositePermissionDTO1).isNotEqualTo(compositePermissionDTO2);
        compositePermissionDTO1.setId(null);
        assertThat(compositePermissionDTO1).isNotEqualTo(compositePermissionDTO2);
    }
}
