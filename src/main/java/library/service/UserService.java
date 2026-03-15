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
import library.model.Role;
import library.model.User;
import library.repository.UserRepository;
import library.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private static final String USER_WITH_ID_NOT_FOUND_MESSAGE = "User is not found with id: ";
    private static final String USER_WITH_EMAIL_EXISTS_MESSAGE
            = "User already exist with email: ";
    private static final String USER_WITH_NAME_EXISTS_MESSAGE = "User already exist with name: ";
    private static final String PASSWORD_IS_NULL_MESSAGE = "Password required";

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
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
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new ConflictException(USER_WITH_EMAIL_EXISTS_MESSAGE + userDto.getEmail());
        }
        if (userRepository.existsByName(userDto.getName())) {
            throw new ConflictException(USER_WITH_NAME_EXISTS_MESSAGE + userDto.getName());
        }
        if (userDto.getPassword() == null || userDto.getPassword().isEmpty()) {
            throw new PasswordRequiredException(PASSWORD_IS_NULL_MESSAGE);
        }

        User userEntity = UserMapper.fromDto(userDto);
        userEntity.setPassword(passwordEncoder.encode(userDto.getPassword()));

        userEntity = userRepository.save(userEntity);

        AuthorizationResponse response = new AuthorizationResponse();
        response.setUserId(userEntity.getId());
        response.setName(userEntity.getName());
        response.setEmail(userEntity.getEmail());
        response.setRole(userEntity.getRole().name());
        response.setToken(jwtUtil.generateToken(userEntity.getEmail(), userEntity.getRole().name()));

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

        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            userEntity.setPassword(passwordEncoder.encode(userDto.getPassword()));
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
        String login = request.getLogin();

        User user = userRepository.findByEmail(login);
        if (user == null) {
            user = userRepository.findByName(login);
        }

        if (user == null) {
            throw new NotFoundException("Пользователь не найден: " + login);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticationException("Неверный пароль");
        }

        AuthorizationResponse response = new AuthorizationResponse();
        response.setUserId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        response.setToken(jwtUtil.generateToken(user.getEmail(), user.getRole().name()));

        return response;
    }
}
