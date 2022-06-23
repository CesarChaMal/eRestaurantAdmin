package com.erestaurant.admin.service.mapper;

import com.erestaurant.admin.domain.SimplePermission;
import com.erestaurant.admin.service.dto.SimplePermissionDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link SimplePermission} and its DTO {@link SimplePermissionDTO}.
 */
@Mapper(componentModel = "spring")
public interface SimplePermissionMapper extends EntityMapper<SimplePermissionDTO, SimplePermission> {}
