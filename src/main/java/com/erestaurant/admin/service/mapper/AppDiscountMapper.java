package com.erestaurant.admin.service.mapper;

import com.erestaurant.admin.domain.AppDiscount;
import com.erestaurant.admin.service.dto.AppDiscountDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link AppDiscount} and its DTO {@link AppDiscountDTO}.
 */
@Mapper(componentModel = "spring")
public interface AppDiscountMapper extends EntityMapper<AppDiscountDTO, AppDiscount> {}
