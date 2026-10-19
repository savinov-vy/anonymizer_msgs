package ru.network.anonymizer_msgs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.network.anonymizer_msgs.entity.MafiaPlayerBlack;



@Repository
public interface MafiaPlayerBlackRepository extends JpaRepository<MafiaPlayerBlack, Long> {
    MafiaPlayerBlack findUserById(Long id);

}
