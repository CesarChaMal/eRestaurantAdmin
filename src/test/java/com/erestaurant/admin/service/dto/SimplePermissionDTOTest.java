package com.erestaurant.admin.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.erestaurant.admin.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SimplePermissionDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(SimplePermissionDTO.class);
        SimplePermissionDTO simplePermissionDTO1 = new SimplePermissionDTO();
        simplePermissionDTO1.setId("id1");
        SimplePermissionDTO simplePermissionDTO2 = new SimplePermissionDTO();
        assertThat(simplePermissionDTO1).isNotEqualTo(simplePermissionDTO2);
        simplePermissionDTO2.setId(simplePermissionDTO1.getId());
        assertThat(simplePermissionDTO1).isEqualTo(simplePermissionDTO2);
        simplePermissionDTO2.setId("id2");
        assertThat(simplePermissionDTO1).isNotEqualTo(simplePermissionDTO2);
        simplePermissionDTO1.setId(null);
        assertThat(simplePermissionDTO1).isNotEqualTo(simplePermissionDTO2);
    }
}
