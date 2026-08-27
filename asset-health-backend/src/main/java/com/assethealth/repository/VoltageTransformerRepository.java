package com.assethealth.repository;

import com.assethealth.entity.VoltageTransformer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoltageTransformerRepository extends JpaRepository<VoltageTransformer, Long> {
    @Query("SELECT COUNT(vt) FROM VoltageTransformer vt")
    long countAll();

    @Query("SELECT COUNT(vt) FROM VoltageTransformer vt WHERE vt.phase = 'A'")
    long countPhaseA();

    @Query("SELECT COUNT(vt) FROM VoltageTransformer vt WHERE vt.phase = 'B'")
    long countPhaseB();

    @Query("SELECT COUNT(vt) FROM VoltageTransformer vt WHERE vt.phase = 'C'")
    long countPhaseC();

    @Query(value = "SELECT vt.phase, COUNT(*) FROM voltage_transformer vt GROUP BY vt.phase ORDER BY vt.phase", nativeQuery = true)
    List<Object[]> countByPhase();
}
