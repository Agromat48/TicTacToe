package org.example.domain.service;

import org.example.datasource.mapper.DataUserMapper;
import org.example.datasource.repository.UserRepository;
import org.example.domain.exception.UserAlreadyExistsException;
import org.example.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public class UserService {
    private final UserRepository userRepository;
    private final DataUserMapper dataUserMapper;

    public UserService(UserRepository userRepository, DataUserMapper dataUserMapper) {
        this.userRepository = userRepository;
        this.dataUserMapper = dataUserMapper;
    }

    public boolean existsByLogin(String login) {
        return userRepository.findByLogin(login).isPresent();
    }

    public User createUser(String login, String password) {
        if (existsByLogin(login)) {
            throw new UserAlreadyExistsException("User with login '" + login + "' already exists");
        }

        User user = new User(UUID.randomUUID(), login, password);
        userRepository.save(dataUserMapper.toDataSource(user));

        return user;
    }

    public Optional<User> findByLogin(String login) {
        return userRepository.findByLogin(login).map(dataUserMapper::toDomain);
    }

    public Optional<User> findByUuid(UUID uuid) {
        return userRepository.findById(uuid).map(dataUserMapper::toDomain);
    }
}