package com.assethealth.repository;

import com.assethealth.entity.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganisationRepository extends JpaRepository<Organisation, Long> {

    @Query(value = """
        SELECT DISTINCT o.id, o.name, o.short_name
        FROM organisation o
        JOIN substation s ON s.owner_id = o.id AND s.status = 2
        JOIN voltage_level vl ON vl.owner_id = s.id AND vl.status = 2
        JOIN bay b ON b.owner_id = vl.id AND b.owner_type = 'VOLTAGE_LEVEL' AND b.status = 2
        JOIN asset_info ai ON ai.owner_id = b.id AND ai.status = 2
        JOIN transformer t ON t.asset_info_id = ai.id AND t.status = 2
        WHERE o.status = 2
        ORDER BY o.short_name
        """, nativeQuery = true)
    List<Object[]> findOrganisationsWithTransformers();
}
