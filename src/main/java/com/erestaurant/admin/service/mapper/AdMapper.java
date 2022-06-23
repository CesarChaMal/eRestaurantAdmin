package com.erestaurant.admin.service.mapper;

import com.erestaurant.admin.domain.Ad;
import com.erestaurant.admin.service.dto.AdDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Ad} and its DTO {@link AdDTO}.
 */
@Mapper(componentModel = "spring")
public interface AdMapper extends EntityMapper<AdDTO, Ad> {}
