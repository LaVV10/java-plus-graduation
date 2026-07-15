package ru.practicum.main.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.main.dto.user.UserDto;
import ru.practicum.main.dto.user.UserShortDto;
import ru.practicum.main.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public User toUserModel(UserDto userDto) {
        if (userDto == null) {
            return null;
        }

        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        return user;
    }

    public UserDto toUserDto(User user) {
        if (user == null) {
            return null;
        }

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        return dto;
    }

    public UserShortDto toUserShortDto(User user) {
        if (user == null) {
            return null;
        }

        UserShortDto dto = new UserShortDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        return dto;
    }

    public List<UserDto> toUserDtoList(List<User> usersList) {
        if (usersList == null) {
            return null;
        }

        return usersList.stream()
                .map(this::toUserDto)
                .collect(Collectors.toList());
    }
}