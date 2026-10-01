package org.example.datasource.repository;

import org.example.datasource.model.DataGame;
import org.example.datasource.model.DataPlayerWinRatio;
import org.example.domain.model.GameStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface GameRepository extends CrudRepository<DataGame, UUID> {
    List<DataGame> findAllByStatus(GameStatus status);

    @Query("""
            SELECT g FROM DataGame g
            WHERE (g.firstPlayerUuid = :userUuid OR g.secondPlayerUuid = :userUuid)
              AND ((g.status = org.example.domain.model.GameStatus.WIN AND g.winnerUuid = :userUuid)
                   OR g.status = org.example.domain.model.GameStatus.DRAW)
            ORDER BY g.createdAt DESC
            """)
    List<DataGame> findFinishedGamesByUserUuid(@Param("userUuid") UUID userUuid);

    @Query(value = """
            WITH player_games AS (
                SELECT first_player_uuid AS user_uuid, status, winner_uuid
                FROM game
                WHERE status IN ('WIN', 'DRAW')
                UNION ALL
                SELECT second_player_uuid, status, winner_uuid
                FROM game
                WHERE status IN ('WIN', 'DRAW') AND second_player_uuid IS NOT NULL
            ),
            stats AS (
                SELECT user_uuid,
                       COUNT(*) FILTER (WHERE status = 'WIN' AND winner_uuid = user_uuid) AS wins,
                       COUNT(*) AS total
                FROM player_games
                GROUP BY user_uuid
            )
            SELECT user_uuid AS "userUuid",
                   CAST(wins AS double precision) / GREATEST(total - wins, 1) AS "winRatio"
            FROM stats
            ORDER BY "winRatio" DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<DataPlayerWinRatio> findTopPlayersByWinRatio(@Param("limit") int limit);
}
