package ru.practicum.ewm.user.service;

import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;

import java.util.List;

public interface UserService {
    //создание пользователя
    UserDto createUser(NewUserRequest newUserRequest);

    //удаление пользователя
    void deleteUser(Long userId);

    //получение информации о пользователях
    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    //получение пользователя по id
    UserDto getUserById(Long userId);

}
