package org.example.web.mapper;

import org.example.domain.model.User;
import org.example.web.model.WebUser;

public class WebUserMapper {
    public WebUser toWeb(User user) {
        return new WebUser(user.getUuid(), user.getLogin());
    }
}