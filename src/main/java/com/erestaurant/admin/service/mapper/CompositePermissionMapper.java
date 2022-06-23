package com.erestaurant.admin.service.mapper;

import com.erestaurant.admin.domain.CompositePermission;
import com.erestaurant.admin.service.dto.CompositePermissionDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link CompositePermission} and its DTO {@link CompositePermissionDTO}.
 */
@Mapper(componentModel = "spring")
public interface CompositePermissionMapper extends EntityMapper<CompositePermissionDTO, CompositePermission> {}
