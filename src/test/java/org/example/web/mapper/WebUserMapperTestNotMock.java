package org.example.web.mapper;

import org.example.domain.model.Role;
import org.example.domain.model.User;
import org.example.web.model.WebUser;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class WebUserMapperTestNotMock {

    @Test
    void toWeb_mapsUuidLoginAndRoles() {
        UUID uuid = UUID.randomUUID();
        User user = new User(uuid, "user", "hash", List.of(Role.USER));

        WebUser webUser = new WebUserMapper().toWeb(user);

        assertAll(
                () -> assertEquals(uuid, webUser.getUuid()),
                () -> assertEquals("user", webUser.getLogin()),
                () -> assertEquals(List.of("USER"), webUser.getRoles())
        );
    }
}
