package ru.network.anonymizer_msgs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.network.anonymizer_msgs.entity.MafiaPlayerRed;


@Repository
public interface MafiaPlayerRedRepository extends JpaRepository<MafiaPlayerRed, Long> {
    MafiaPlayerRed findUserById(Long id);

}
