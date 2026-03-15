package library.mapper;

import library.dto.create.UserCreateDto;
import library.dto.get.UserGetDto;
import library.model.Role;
import library.model.User;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UserMapper {
    public UserGetDto toDto(User user) {
        UserGetDto dto = new UserGetDto();

        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());

        return dto;
    }

    public User fromDto(UserCreateDto dto) {
        User entity = new User();

        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setPassword(dto.getPassword());
        entity.setRole(Role.USER); // Default role

        return entity;
    }
}
