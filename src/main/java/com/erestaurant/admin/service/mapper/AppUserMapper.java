package com.erestaurant.admin.service.mapper;

import com.erestaurant.admin.domain.AppUser;
import com.erestaurant.admin.domain.User;
import com.erestaurant.admin.service.dto.AppUserDTO;
import com.erestaurant.admin.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link AppUser} and its DTO {@link AppUserDTO}.
 */
@Mapper(componentModel = "spring")
public interface AppUserMapper extends EntityMapper<AppUserDTO, AppUser> {
    @Mapping(target = "internalUser", source = "internalUser", qualifiedByName = "userLogin")
    AppUserDTO toDto(AppUser s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
