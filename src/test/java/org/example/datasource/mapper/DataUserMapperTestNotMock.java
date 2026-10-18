package org.example.datasource.mapper;

import org.example.datasource.model.DataUser;
import org.example.domain.model.Role;
import org.example.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataUserMapperTestNotMock {

    private DataUserMapper dataUserMapper;

    @BeforeEach
    void setUp() {
        dataUserMapper = new DataUserMapper();
    }

    @Test
    void toDomain_mapsAllFields() {
        UUID uuid = UUID.randomUUID();
        DataUser dataUser = new DataUser(uuid, "user", "hash", List.of(Role.USER));

        User user = dataUserMapper.toDomain(dataUser);

        assertAll(
                () -> assertEquals(uuid, user.getUuid()),
                () -> assertEquals("user", user.getLogin()),
                () -> assertEquals("hash", user.getPassword()),
                () -> assertEquals(List.of(Role.USER), user.getRoles())
        );
    }

    @Test
    void toDataSource_mapsAllFields() {
        UUID uuid = UUID.randomUUID();
        User user = new User(uuid, "user", "hash", List.of(Role.USER));

        DataUser dataUser = dataUserMapper.toDataSource(user);

        assertAll(
                () -> assertEquals(uuid, dataUser.getUuid()),
                () -> assertEquals("user", dataUser.getLogin()),
                () -> assertEquals("hash", dataUser.getPassword()),
                () -> assertEquals(List.of(Role.USER), dataUser.getRoles())
        );
    }

    @Test
    void roundTrip_userToDataSourceToDomain_keepsAllFields() {
        User original = new User(UUID.randomUUID(), "user", "hash", List.of(Role.USER));

        User result = dataUserMapper.toDomain(dataUserMapper.toDataSource(original));

        assertAll(
                () -> assertEquals(original.getUuid(), result.getUuid()),
                () -> assertEquals(original.getLogin(), result.getLogin()),
                () -> assertEquals(original.getPassword(), result.getPassword()),
                () -> assertEquals(original.getRoles(), result.getRoles())
        );
    }
}
