package com.erestaurant.admin.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.erestaurant.admin.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AppDiscountDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(AppDiscountDTO.class);
        AppDiscountDTO appDiscountDTO1 = new AppDiscountDTO();
        appDiscountDTO1.setId("id1");
        AppDiscountDTO appDiscountDTO2 = new AppDiscountDTO();
        assertThat(appDiscountDTO1).isNotEqualTo(appDiscountDTO2);
        appDiscountDTO2.setId(appDiscountDTO1.getId());
        assertThat(appDiscountDTO1).isEqualTo(appDiscountDTO2);
        appDiscountDTO2.setId("id2");
        assertThat(appDiscountDTO1).isNotEqualTo(appDiscountDTO2);
        appDiscountDTO1.setId(null);
        assertThat(appDiscountDTO1).isNotEqualTo(appDiscountDTO2);
    }
}
