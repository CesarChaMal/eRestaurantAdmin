package com.erestaurant.admin.service.mapper;

import com.erestaurant.admin.domain.Permission;
import com.erestaurant.admin.service.dto.PermissionDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Permission} and its DTO {@link PermissionDTO}.
 */
@Mapper(componentModel = "spring")
public interface PermissionMapper extends EntityMapper<PermissionDTO, Permission> {}
