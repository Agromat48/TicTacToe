package org.example.domain.service;

import org.example.datasource.mapper.DataUserMapper;
import org.example.datasource.model.DataUser;
import org.example.datasource.repository.UserRepository;
import org.example.domain.exception.UserAlreadyExistsException;
import org.example.domain.model.Role;
import org.example.domain.model.User;

import java.util.List;
import org.example.domain.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class UserServiceTestMock {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Spy
    private DataUserMapper userMapper = new DataUserMapper();

    @Test
    void existsByLogin_returnTrue() {
        DataUser user = createUser("user", "12345");
        Mockito.when(userRepository.findByLogin("user"))
                .thenReturn(Optional.of(user));

        Assertions.assertTrue(userService.existsByLogin("user"));
    }

    @Test
    void existsByLogin_returnFalse() {
        Mockito.when(userRepository.findByLogin("user"))
                .thenReturn(Optional.empty());

        Assertions.assertFalse(userService.existsByLogin("user"));
    }

    @Test
    void createUser_returnThrow_whenExistsByLoginTrue() {
        Mockito.when(userRepository.findByLogin("user"))
                .thenReturn(Optional.of(createUser("user", "12345")));

        UserAlreadyExistsException exception = Assertions.assertThrows(UserAlreadyExistsException.class,
                () -> userService.createUser("user", "12345"));

        Assertions.assertEquals("User with login 'user' already exists", exception.getMessage());
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void createUser_returnUser_whenExistsByLoginFalse() {
        Mockito.when(userRepository.findByLogin("user"))
                .thenReturn(Optional.empty());

        User result = userService.createUser("user", "12345");

        Assertions.assertNotNull(result.getUuid());
        Assertions.assertEquals("user", result.getLogin());
        Assertions.assertEquals("12345", result.getPassword());

        Mockito.verify(userRepository, Mockito.times(1)).findByLogin("user");
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(DataUser.class));
    }

    @Test
    void findByLogin_returnPresent() {
        Mockito.when(userRepository.findByLogin("user"))
                .thenReturn(Optional.of(createUser("user", "12345")));

        Optional<User> user = userService.findByLogin("user");


        Assertions.assertTrue(user.isPresent());
        Assertions.assertEquals(user.get().getLogin(), "user");
        Assertions.assertEquals(user.get().getPassword(), "12345");

        Mockito.verify(userRepository, Mockito.times(1)).findByLogin("user");
    }

    @Test
    void findByLogin_returnEmpty() {
        Mockito.when(userRepository.findByLogin("user"))
                .thenReturn(Optional.empty());

        Optional<User> user = userService.findByLogin("user");

        Assertions.assertTrue(user.isEmpty());

        Mockito.verify(userRepository, Mockito.times(1)).findByLogin("user");
    }

    @Test
    void findByUuid_returnPresent() {
        UUID uuid = UUID.randomUUID();
        Mockito.when(userRepository.findById(uuid))
                .thenReturn(Optional.of(createUser("user", "12345")));

        Optional<User> user = userService.findByUuid(uuid);


        Assertions.assertTrue(user.isPresent());
        Assertions.assertEquals(user.get().getLogin(), "user");
        Assertions.assertEquals(user.get().getPassword(), "12345");

        Mockito.verify(userRepository, Mockito.times(1)).findById(uuid);
    }

    @Test
    void findByUuid_returnEmpty() {
        UUID uuid = UUID.randomUUID();
        Mockito.when(userRepository.findById(uuid))
                .thenReturn(Optional.empty());

        Optional<User> user = userService.findByUuid(uuid);

        Assertions.assertTrue(user.isEmpty());

        Mockito.verify(userRepository, Mockito.times(1)).findById(uuid);
    }

    private DataUser createUser(String login, String password) {
        return new DataUser(
                UUID.randomUUID(),
                login,
                password,
                List.of(Role.USER)
        );
    }

}
