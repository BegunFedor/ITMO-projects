package ru.itmo.mkd.mapper;

import org.mapstruct.Mapper;
import ru.itmo.mkd.dto.auth.UserDto;
import ru.itmo.mkd.entity.AppUser;

@Mapper
public interface UserMapper {

    UserDto toDto(AppUser user);
}
