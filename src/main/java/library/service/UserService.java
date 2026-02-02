package library.service;

import java.util.List;
import java.util.Objects;
import library.dto.AuthorizationRequest;
import library.dto.AuthorizationResponse;
import library.dto.create.UserCreateDto;
import library.dto.get.UserGetDto;
import library.exception.AuthenticationException;
import library.exception.ConflictException;
import library.exception.NotFoundException;
import library.exception.PasswordRequiredException;
import library.mapper.UserMapper;
import library.model.User;
import library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private static final String USER_WITH_ID_NOT_FOUND_MESSAGE = "User is not found with id: ";
    private static final String USER_WITH_EMAIL_NOT_FOUND_MESSAGE
            = "User is not found with email: ";
    private static final String USER_WITH_EMAIL_EXISTS_MESSAGE
            = "User already exist with email: ";
    private static final String USER_WITH_NAME_EXISTS_MESSAGE = "User already exist with name: ";
    private static final String PASSWORD_IS_NULL_MESSAGE = "Password required";

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserGetDto> getAllUsers() {
        return userRepository.findAll().stream().map(UserMapper::toDto)
                .toList();
    }

    public UserGetDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_WITH_ID_NOT_FOUND_MESSAGE + id));
        return UserMapper.toDto(user);
    }

    public AuthorizationResponse createUser(UserCreateDto userDto) {
        User userEntity = UserMapper.fromDto(userDto);

        if (userRepository.existsByEmail(userEntity.getEmail())) {
            throw new ConflictException(USER_WITH_EMAIL_EXISTS_MESSAGE + userEntity.getEmail());
        }
        if (userRepository.existsByName(userEntity.getName())) {
            throw new ConflictException(USER_WITH_NAME_EXISTS_MESSAGE + userEntity.getEmail());
        }

        if (userEntity.getPassword() == null) {
            throw new PasswordRequiredException(PASSWORD_IS_NULL_MESSAGE);
        }

        userEntity = userRepository.save(userEntity);

        AuthorizationResponse response = new AuthorizationResponse();
        response.setUserId(userEntity.getId());
        response.setName(userEntity.getName());
        response.setEmail(userEntity.getEmail());
        response.setToken("dummy-token");

        return response;
    }

    public UserGetDto updateUser(Long id, UserCreateDto userDto) {
        User userEntity = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_WITH_ID_NOT_FOUND_MESSAGE + id));
        User userWithEmail = userRepository.findByEmail(userDto.getEmail());
        User userWithName = userRepository.findByName(userDto.getName());

        if (userWithEmail != null && !Objects.equals(userWithEmail.getId(), userEntity.getId())) {
            throw new ConflictException(USER_WITH_EMAIL_EXISTS_MESSAGE + userEntity.getEmail());
        }
        if (userWithName != null && !Objects.equals(userWithName.getId(), userEntity.getId())) {
            throw new ConflictException(USER_WITH_NAME_EXISTS_MESSAGE + userEntity.getName());
        }

        if (userDto.getPassword() != null) {
            userEntity.setPassword(userDto.getPassword());
        }

        userEntity.setEmail(userDto.getEmail());
        userEntity.setName(userDto.getName());
        return UserMapper.toDto(userRepository.save(userEntity));
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException(USER_WITH_ID_NOT_FOUND_MESSAGE + id);
        }
        userRepository.deleteById(id);
    }

    public AuthorizationResponse authenticate(AuthorizationRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            throw new NotFoundException(USER_WITH_EMAIL_NOT_FOUND_MESSAGE + request.getEmail());
        }

        if (!user.getPassword().equals(request.getPassword())) {
            throw new AuthenticationException("Неверный пароль");
        }

        AuthorizationResponse response = new AuthorizationResponse();
        response.setUserId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setToken("dummy-token");

        return response;
    }
}
