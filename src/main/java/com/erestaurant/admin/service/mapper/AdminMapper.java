package com.erestaurant.admin.service.mapper;

import com.erestaurant.admin.domain.Admin;
import com.erestaurant.admin.service.dto.AdminDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Admin} and its DTO {@link AdminDTO}.
 */
@Mapper(componentModel = "spring")
public interface AdminMapper extends EntityMapper<AdminDTO, Admin> {}
