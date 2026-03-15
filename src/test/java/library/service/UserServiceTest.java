package library.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import library.dto.create.UserCreateDto;
import library.dto.create.UserRoleUpdateDto;
import library.dto.get.UserGetDto;
import library.exception.ConflictException;
import library.exception.NotFoundException;
import library.model.Book;
import library.model.Review;
import library.model.Role;
import library.model.User;
import library.repository.UserRepository;
import library.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    private final Book bookTest = new Book(1L, "Test Book",
            null, null, 100, null, 1000, null);
    private final Review reviewTest = new Review(1L, bookTest,
            null, 2, "Comment");
    private final User userTest = new User(1L, "Test User",
            "Password", "email@gmail.com", Role.USER, List.of(reviewTest));

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userTest));

        UserGetDto result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(userTest.getName(), result.getName());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_WhenNotFound_ShouldReturnNull() {
        when(userRepository.findById(20L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.getUserById(20L));

        assertEquals("User is not found with id: " + 20L, exception.getMessage());
    }

    @Test
    void createUser_WithValidData_ShouldCreateUser() {
        UserCreateDto userDto = new UserCreateDto("New User", "da@gmail.com", "1111");
        User savedUser = new User(2L, userDto.getName(), userDto.getPassword(),
                userDto.getEmail(), Role.USER, null);

        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        library.dto.AuthorizationResponse result = userService.createUser(userDto);

        assertNotNull(result);
        assertEquals(userDto.getEmail(), result.getEmail());
        assertEquals(userDto.getName(), result.getName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_WithValidData_ShouldUpdateUser() {
        UserCreateDto userDto = new UserCreateDto("Updated User", "da@gmail.com", "1111");

        when(userRepository.findById(1L)).thenReturn(Optional.of(userTest));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(userTest);

        UserGetDto result = userService.updateUser(1L, userDto);

        assertEquals(userDto.getEmail(), result.getEmail());
        assertEquals(userDto.getName(), result.getName());
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_WhenNotFound_ShouldThrowException() {
        UserCreateDto userDto = new UserCreateDto("Updated User", "da@gmail.com", "1111");

        when(userRepository.findById(20L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> userService.updateUser(20L, userDto));
    }

    @Test
    void updateUserRole_WithValidRole_ShouldUpdateRole() {
        UserRoleUpdateDto roleDto = new UserRoleUpdateDto();
        roleDto.setRole("ADMIN");

        when(userRepository.findById(1L)).thenReturn(Optional.of(userTest));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("other@gmail.com"); // Not self
        when(userRepository.save(any(User.class))).thenReturn(userTest);

        UserGetDto result = userService.updateUserRole(1L, roleDto);

        assertNotNull(result);
        assertEquals("ADMIN", result.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deleteUser_WhenUserExistsAndNotSelf_ShouldDeleteUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userTest));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("other@gmail.com"); // Not self
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_WhenTryingToDeleteSelf_ShouldThrowConflictException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userTest));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userTest.getEmail()); // Self delete!

        ConflictException exception = assertThrows(ConflictException.class, () -> userService.deleteUser(1L));
        assertEquals("Вы не можете удалить собственный аккаунт", exception.getMessage());
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteUser_WhenNotFound_ShouldThrowException() {
        when(userRepository.findById(20L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.deleteUser(20L));
    }

    @Test
    void getAllUsers_ReturnsList() {
        User anotherUserTest = new User(2L, "Another Test User", "Password",
                "email@gmail.com", Role.USER, List.of(reviewTest));

        when(userRepository.findAll()).thenReturn(List.of(userTest, anotherUserTest));

        List<UserGetDto> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("Test User", result.get(0).getName());
        assertEquals("Another Test User", result.get(1).getName());
        verify(userRepository).findAll();
    }
}
