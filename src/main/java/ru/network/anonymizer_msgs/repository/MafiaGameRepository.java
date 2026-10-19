package ru.network.anonymizer_msgs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.network.anonymizer_msgs.entity.MafiaGame;

import java.util.List;


@Repository
public interface MafiaGameRepository extends JpaRepository<MafiaGame, Long> {
    @Query("""
            select g from MafiaGame g
            where g.endGameAt is null
            order by g.startGameAt desc nulls last
            """)
    List<MafiaGame> findOpenGames();

    @Query("""
            select g from MafiaGame g
            order by g.startGameAt desc nulls last
            """)
    List<MafiaGame> findLastGames();

    default MafiaGame findLastOpen() {
        return findOpenGames().stream().findFirst().orElse(null);
    }

    /**
     * для закрытия игры
     * т.к. закрою игру в боте игры и тут на всякий случай тоже
     * @return
     */
    default MafiaGame findLast() {
        return findLastGames().stream().findFirst().orElse(null);
    }
}
