package com.smartvehicle.repository;

import java.util.Optional;

import com.smartvehicle.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);


    Boolean existsByUsername(String username);

    Boolean existsUserByUsername(String username);

//    @Modifying
//    @Query("UPDATE User u SET u.token = :token WHERE u.username = :username")
//    int updateTokenByUsername(@Param("username") String username, @Param("token") String token);
}
