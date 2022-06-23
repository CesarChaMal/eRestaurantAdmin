package com.erestaurant.admin.service.mapper;

import com.erestaurant.admin.domain.Discount;
import com.erestaurant.admin.service.dto.DiscountDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Discount} and its DTO {@link DiscountDTO}.
 */
@Mapper(componentModel = "spring")
public interface DiscountMapper extends EntityMapper<DiscountDTO, Discount> {}
