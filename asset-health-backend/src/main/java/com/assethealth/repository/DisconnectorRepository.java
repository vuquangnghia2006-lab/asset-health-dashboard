package com.assethealth.repository;

import com.assethealth.entity.Disconnector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DisconnectorRepository extends JpaRepository<Disconnector, Long> {
    @Query("SELECT COUNT(d) FROM Disconnector d")
    long countAll();
}
