package com.smart_vehicle.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smart_vehicle.models.Parent;

@Repository
public interface ParentRepository extends JpaRepository<Parent, String> {

    Optional<Parent> findByUserName(String username);

    Boolean existsByUserName(String username);

    Boolean existsByEmail(String email);

    Boolean existsUserByUserName(String username);
}
