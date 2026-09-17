package org.example.datasource.repository;

import org.example.datasource.model.DataGame;
import org.example.domain.model.Game;
import org.example.domain.model.GameStatus;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface GameRepository extends CrudRepository<DataGame, UUID> {
    List<DataGame> findAllByStatus(GameStatus status);
}
