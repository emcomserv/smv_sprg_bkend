package com.smartvehicle.repository;

import com.smartvehicle.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public  interface AdminRepository extends JpaRepository<Admin,Long> {
    Optional<Admin> findByUser_Id(Long userId);
}