package com.erestaurant.admin.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.erestaurant.admin.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DiscountTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Discount.class);
        Discount discount1 = new Discount();
        discount1.setId("id1");
        Discount discount2 = new Discount();
        discount2.setId(discount1.getId());
        assertThat(discount1).isEqualTo(discount2);
        discount2.setId("id2");
        assertThat(discount1).isNotEqualTo(discount2);
        discount1.setId(null);
        assertThat(discount1).isNotEqualTo(discount2);
    }
}
