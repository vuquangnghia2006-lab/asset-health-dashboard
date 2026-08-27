package com.assethealth.repository;

import com.assethealth.entity.ShortCircuitImpedance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShortCircuitImpedanceRepository extends JpaRepository<ShortCircuitImpedance, Long> {
    List<ShortCircuitImpedance> findByTransformerIdAndStatus(Long transformerId, Long status);

    @Query(value = "SELECT sci.impedance, sci.transformer_id FROM short_circuit_impedance sci WHERE sci.status = 1", nativeQuery = true)
    List<Object[]> findAllImpedances();
}
