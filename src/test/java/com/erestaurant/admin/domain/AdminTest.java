package com.erestaurant.admin.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.erestaurant.admin.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AdminTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Admin.class);
        Admin admin1 = new Admin();
        admin1.setId("id1");
        Admin admin2 = new Admin();
        admin2.setId(admin1.getId());
        assertThat(admin1).isEqualTo(admin2);
        admin2.setId("id2");
        assertThat(admin1).isNotEqualTo(admin2);
        admin1.setId(null);
        assertThat(admin1).isNotEqualTo(admin2);
    }
}
