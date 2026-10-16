package ru.network.anonymizer_msgs.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import ru.network.anonymizer_msgs.entity.User;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findUserById(Long id);

}
