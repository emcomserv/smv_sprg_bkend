package com.smart_vehicle.repository;

import java.util.Optional;

import com.smart_vehicle.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsUserByUsername(String username);
}
