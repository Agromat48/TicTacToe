package org.example.datasource.mapper;

import org.example.datasource.model.DataUser;
import org.example.domain.model.User;

public class DataUserMapper {
    public User toDomain(DataUser dataUser) {
        return new User(dataUser.getUuid(), dataUser.getLogin(), dataUser.getPassword());
    }

    public DataUser toDataSource(User user) {
        return new DataUser(user.getUuid(), user.getLogin(), user.getPassword());
    }
}