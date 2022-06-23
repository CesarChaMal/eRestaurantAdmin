package com.erestaurant.admin.service.mapper;

import com.erestaurant.admin.domain.Role;
import com.erestaurant.admin.service.dto.RoleDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Role} and its DTO {@link RoleDTO}.
 */
@Mapper(componentModel = "spring")
public interface RoleMapper extends EntityMapper<RoleDTO, Role> {}
