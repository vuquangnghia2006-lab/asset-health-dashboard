package com.assethealth.repository;

import com.assethealth.entity.CurrentTransformer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CurrentTransformerRepository extends JpaRepository<CurrentTransformer, Long> {
    @Query("SELECT COUNT(ct) FROM CurrentTransformer ct")
    long countAll();

    @Query("SELECT COUNT(ct) FROM CurrentTransformer ct WHERE ct.phase = 'A'")
    long countPhaseA();

    @Query("SELECT COUNT(ct) FROM CurrentTransformer ct WHERE ct.phase = 'B'")
    long countPhaseB();

    @Query("SELECT COUNT(ct) FROM CurrentTransformer ct WHERE ct.phase = 'C'")
    long countPhaseC();

    @Query(value = "SELECT ct.phase, COUNT(*) FROM current_transformer ct GROUP BY ct.phase ORDER BY ct.phase", nativeQuery = true)
    List<Object[]> countByPhase();
}
