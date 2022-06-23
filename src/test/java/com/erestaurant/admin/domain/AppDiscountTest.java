package com.erestaurant.admin.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.erestaurant.admin.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AppDiscountTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AppDiscount.class);
        AppDiscount appDiscount1 = new AppDiscount();
        appDiscount1.setId("id1");
        AppDiscount appDiscount2 = new AppDiscount();
        appDiscount2.setId(appDiscount1.getId());
        assertThat(appDiscount1).isEqualTo(appDiscount2);
        appDiscount2.setId("id2");
        assertThat(appDiscount1).isNotEqualTo(appDiscount2);
        appDiscount1.setId(null);
        assertThat(appDiscount1).isNotEqualTo(appDiscount2);
    }
}
