package com.smartvehicle.repository;

import com.smartvehicle.entity.Attender;
import com.smartvehicle.entity.Parent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttenderRepository extends JpaRepository<Attender, Long> {

    Parent findByUser_Id(Long userId);
}
