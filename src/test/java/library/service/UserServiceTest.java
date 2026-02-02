package library.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import library.dto.create.UserCreateDto;
import library.dto.get.UserGetDto;
import library.exception.NotFoundException;
import library.model.Book;
import library.model.Review;
import library.model.User;
import library.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private final Book bookTest = new Book(1L, "Test Book",
            null, null, 100, null, 1000, null, null);
    private final Review reviewTest = new Review(1L, bookTest,
            null, 2, "Comment");
    private final User userTest = new User(1L, "Test User",
            "Password", "email@gmail.com", List.of(reviewTest), Set.of(bookTest));

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
                userDto.getEmail(), null, null);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserGetDto result = userService.createUser(userDto);

        assertNotNull(result);
        assertEquals(userDto.getEmail(), result.getEmail());
        assertEquals(userDto.getName(), result.getName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_WithValidData_ShouldUpdateUser() {
        UserCreateDto userDto = new UserCreateDto("Updated User", "da@gmail.com", "1111");

        when(userRepository.findById(1L)).thenReturn(Optional.of(userTest));
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
    void deleteUser_WhenUserExists_ShouldDeleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_WhenNotFound_ShouldThrowException() {
        when(userRepository.existsById(20L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> userService.deleteUser(20L));
    }

    @Test
    void getAllUsers_ReturnsList() {
        User anotherUserTest = new User(2L, "Another Test User", "Password",
                "email@gmail.com", List.of(reviewTest), Set.of(bookTest));

        when(userRepository.findAll()).thenReturn(List.of(userTest, anotherUserTest));

        List<UserGetDto> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("Test User", result.get(0).getName());
        assertEquals("Another Test User", result.get(1).getName());
        verify(userRepository).findAll();
    }
}
