package com.assethealth.repository;

import com.assethealth.entity.Substation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubstationRepository extends JpaRepository<Substation, Long> {

    @Query("SELECT COUNT(s) FROM Substation s WHERE s.status = 2")
    long countActive();

    @Query(value = """
        SELECT DISTINCT s.id, s.name, COALESCE(ad.city, '')
        FROM substation s
        JOIN voltage_level vl ON vl.owner_id = s.id AND vl.status = 2
        JOIN bay b ON b.owner_id = vl.id AND b.owner_type = 'VOLTAGE_LEVEL' AND b.status = 2
        JOIN asset_info ai ON ai.owner_id = b.id AND ai.status = 2
        JOIN transformer t ON t.asset_info_id = ai.id AND t.status = 2
        LEFT JOIN address_detail ad ON ad.id = s.address_detail_id
        WHERE s.status = 2
          AND (:organisationId IS NULL OR s.owner_id = :organisationId)
        ORDER BY s.name
        """, nativeQuery = true)
    List<Object[]> findSubstationsWithTransformers(@Param("organisationId") Long organisationId);

    @Query(value = """
        SELECT COALESCE(s.substation_type, 'Unknown') as stype, COUNT(DISTINCT s.id) as cnt
        FROM substation s
        WHERE s.status = 2
        GROUP BY s.substation_type
        ORDER BY cnt DESC
        """, nativeQuery = true)
    List<Object[]> countBySubstationType();

    @Query(value = """
        SELECT COALESCE(s.substation_type, 'Unknown') as stype, COUNT(DISTINCT t.id) as tx_count,
               COALESCE(SUM(pr.rated_power), 0) as total_kva
        FROM substation s
        JOIN voltage_level vl ON vl.owner_id = s.id AND vl.status = 2
        JOIN bay b ON b.owner_id = vl.id AND b.owner_type = 'VOLTAGE_LEVEL' AND b.status = 2
        JOIN asset_info ai ON ai.owner_id = b.id AND ai.status = 2
        JOIN transformer t ON t.asset_info_id = ai.id AND t.status = 2
        LEFT JOIN power_rating pr ON pr.transformer_id = t.id AND pr.status = 2
        WHERE s.status = 2
        GROUP BY s.substation_type
        ORDER BY tx_count DESC
        """, nativeQuery = true)
    List<Object[]> countTransformersBySubstationType();
}
