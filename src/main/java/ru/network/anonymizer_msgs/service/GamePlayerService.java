
package ru.network.anonymizer_msgs.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.network.anonymizer_msgs.entity.MafiaGame;
import ru.network.anonymizer_msgs.entity.MafiaPlayerBlack;
import ru.network.anonymizer_msgs.entity.MafiaPlayerRed;
import ru.network.anonymizer_msgs.model.Player;
import ru.network.anonymizer_msgs.repository.MafiaGameRepository;
import ru.network.anonymizer_msgs.repository.MafiaPlayerBlackRepository;
import ru.network.anonymizer_msgs.repository.MafiaPlayerRedRepository;
import ru.network.anonymizer_msgs.repository.PlayerRepository;
import ru.network.anonymizer_msgs.type.Role;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GamePlayerService {

    PlayerRepository playerRepository;
    MafiaGameRepository mafiaGameRepository;
    MafiaPlayerBlackRepository mafiaPlayerBlackRepository;
    MafiaPlayerRedRepository mafiaPlayerRedRepository;


    @Transactional
    public void createNewGame(List<Player> players) {
        MafiaGame openGame = mafiaGameRepository.findLastOpen();
        if (openGame != null) {
            openGame.setEndGameAt(LocalDateTime.now());
        }
        Set<MafiaPlayerBlack> mafiaPlayerBlacks = new HashSet<>();
        Set<MafiaPlayerRed> mafiaPlayerReds = new HashSet<>();
        MafiaPlayerRed sheriff = null;
        MafiaPlayerBlack donMafia = null;
        for (Player player : players) {
            Long id = player.getId();
            mafiaPlayerRedRepository.deleteById(id);
            mafiaPlayerBlackRepository.deleteById(id);
            if (player.getRole() == Role.SHERIFF) {
                sheriff = mafiaPlayerRedRepository.save(MafiaPlayerRed.builder()
                        .playerId(player.getId())
                        .isSheriff(true)
                        .playerName(player.getName())
                        .build());
                mafiaPlayerReds.add(sheriff);
            } else if (player.getRole() == Role.CIVILIAN) {
                MafiaPlayerRed civilian = mafiaPlayerRedRepository.save(MafiaPlayerRed.builder()
                        .playerId(player.getId())
                        .isSheriff(false)
                        .playerName(player.getName())
                        .build());
                mafiaPlayerReds.add(civilian);
            } else if (player.getRole() == Role.DON) {
                donMafia = mafiaPlayerBlackRepository.save(MafiaPlayerBlack.builder()
                        .playerId(player.getId())
                        .isDon(true)
                        .playerName(player.getName())
                        .build());
                mafiaPlayerBlacks.add(donMafia);
            } else if (player.getRole() == Role.MAFIA) {
                MafiaPlayerBlack mafia = mafiaPlayerBlackRepository.save(MafiaPlayerBlack.builder()
                        .playerId(player.getId())
                        .isDon(false)
                        .playerName(player.getName())
                        .build());
                mafiaPlayerBlacks.add(mafia);
            }
        }
        MafiaGame savedNewMafiaGame = mafiaGameRepository.save(MafiaGame.builder()
                .startGameAt(LocalDateTime.now())
                .donMafiaId(donMafia != null ? donMafia.getId() : null)
                .sheriffId(sheriff != null ? sheriff.getId() : null)
                .isFirstNight(Boolean.TRUE)
                .mafiaPlayerBlacks(mafiaPlayerBlacks)
                .mafiaPlayerReds(mafiaPlayerReds)
                .build());

        for (MafiaPlayerBlack mafiaPlayerBlack : mafiaPlayerBlacks) {
            mafiaPlayerBlack.setMafiaGame(savedNewMafiaGame);
        }
        for (MafiaPlayerRed mafiaPlayerRed : mafiaPlayerReds) {
            mafiaPlayerRed.setMafiaGame(savedNewMafiaGame);
        }
    }

}
