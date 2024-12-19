package ru.practicum.ewm.user.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.dto.UserShortDto;
import ru.practicum.ewm.user.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(NewUserRequest newUserRequest);

    User toEntity(UserDto userDto);

    UserDto toDto(User createUser);

    UserShortDto toDto(UserDto userDto);

}
