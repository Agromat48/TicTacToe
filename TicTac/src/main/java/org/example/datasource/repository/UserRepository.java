package org.example.datasource.repository;

import org.example.datasource.model.DataUser;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends CrudRepository<DataUser, UUID> {
    Optional<DataUser> findByLogin(String login);
}
