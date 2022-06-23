package com.erestaurant.admin.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.erestaurant.admin.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AdminDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(AdminDTO.class);
        AdminDTO adminDTO1 = new AdminDTO();
        adminDTO1.setId("id1");
        AdminDTO adminDTO2 = new AdminDTO();
        assertThat(adminDTO1).isNotEqualTo(adminDTO2);
        adminDTO2.setId(adminDTO1.getId());
        assertThat(adminDTO1).isEqualTo(adminDTO2);
        adminDTO2.setId("id2");
        assertThat(adminDTO1).isNotEqualTo(adminDTO2);
        adminDTO1.setId(null);
        assertThat(adminDTO1).isNotEqualTo(adminDTO2);
    }
}
