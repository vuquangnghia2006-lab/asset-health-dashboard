package com.assethealth.repository;

import com.assethealth.entity.Transformer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransformerRepository extends JpaRepository<Transformer, Long> {

    @Query("SELECT COUNT(t) FROM Transformer t WHERE t.status = 2")
    long countActive();

    @Query("SELECT COALESCE(SUM(pr.ratedPower), 0) FROM PowerRating pr WHERE pr.status = 2")
    double sumRatedPowerActive();

    @Query(value = """
        SELECT COALESCE(vl.base_voltage, 0) as kv,
               COALESCE(SUM(pr.rated_power), 0) as total_kva,
               COUNT(DISTINCT t.id) as tx_count
        FROM transformer t
        JOIN asset_info ai ON t.asset_info_id = ai.id
        JOIN bay b ON ai.owner_id = b.id AND b.owner_type = 'VOLTAGE_LEVEL'
        JOIN voltage_level vl ON b.owner_id = vl.id
        LEFT JOIN power_rating pr ON pr.transformer_id = t.id AND pr.status = 2
        LEFT JOIN substation s ON vl.owner_id = s.id
        LEFT JOIN address_detail ad ON s.address_detail_id = ad.id
        WHERE t.status = 2 AND ai.status = 2 AND vl.status = 2
          AND (:organisationId IS NULL OR s.owner_id = :organisationId)
          AND (:manufacturerId IS NULL OR ai.manufacturer_id = :manufacturerId)
          AND (:voltageLevel IS NULL OR vl.base_voltage = :voltageLevel)
          AND (:status IS NULL OR t.status = :status)
          AND (:city IS NULL OR ad.city = :city)
          AND (:ward IS NULL OR ad.ward_commune = :ward)
        GROUP BY vl.base_voltage
        ORDER BY kv DESC
        """, nativeQuery = true)
    List<Object[]> capacityByVoltageLevel(@Param("organisationId") Long organisationId,
                                           @Param("manufacturerId") Long manufacturerId,
                                           @Param("voltageLevel") Integer voltageLevel,
                                           @Param("status") Long status,
                                           @Param("city") String city,
                                           @Param("ward") String ward);

    @Query(value = """
        SELECT COALESCE(m.name, 'Unknown'), COUNT(DISTINCT t.id) as cnt
        FROM transformer t
        JOIN asset_info ai ON t.asset_info_id = ai.id
        JOIN bay b ON ai.owner_id = b.id AND b.owner_type = 'VOLTAGE_LEVEL'
        JOIN voltage_level vl ON b.owner_id = vl.id
        LEFT JOIN manufacturer m ON ai.manufacturer_id = m.id
        LEFT JOIN substation s ON vl.owner_id = s.id
        LEFT JOIN address_detail ad ON s.address_detail_id = ad.id
        WHERE t.status = 2 AND ai.status = 2 AND vl.status = 2
          AND (:organisationId IS NULL OR s.owner_id = :organisationId)
          AND (:manufacturerId IS NULL OR ai.manufacturer_id = :manufacturerId)
          AND (:voltageLevel IS NULL OR vl.base_voltage = :voltageLevel)
          AND (:status IS NULL OR t.status = :status)
          AND (:city IS NULL OR ad.city = :city)
          AND (:ward IS NULL OR ad.ward_commune = :ward)
        GROUP BY m.name
        ORDER BY cnt DESC
        """, nativeQuery = true)
    List<Object[]> countByManufacturer(@Param("organisationId") Long organisationId,
                                        @Param("manufacturerId") Long manufacturerId,
                                        @Param("voltageLevel") Integer voltageLevel,
                                        @Param("status") Long status,
                                        @Param("city") String city,
                                        @Param("ward") String ward);

    @Query(value = """
        SELECT COUNT(t) FROM transformer t
        JOIN asset_info ai ON t.asset_info_id = ai.id
        JOIN bay b ON ai.owner_id = b.id AND b.owner_type = 'VOLTAGE_LEVEL'
        JOIN voltage_level vl ON b.owner_id = vl.id
        LEFT JOIN substation s ON vl.owner_id = s.id
        LEFT JOIN address_detail ad ON s.address_detail_id = ad.id
        WHERE t.status = 2 AND ai.status = 2 AND vl.status = 2
          AND (:organisationId IS NULL OR s.owner_id = :organisationId)
          AND (:manufacturerId IS NULL OR ai.manufacturer_id = :manufacturerId)
          AND (:voltageLevel IS NULL OR vl.base_voltage = :voltageLevel)
          AND (:status IS NULL OR t.status = :status)
          AND (:city IS NULL OR ad.city = :city)
          AND (:ward IS NULL OR ad.ward_commune = :ward)
        """, nativeQuery = true)
    long countActiveFiltered(@Param("organisationId") Long organisationId,
                              @Param("manufacturerId") Long manufacturerId,
                              @Param("voltageLevel") Integer voltageLevel,
                              @Param("status") Long status,
                              @Param("city") String city,
                              @Param("ward") String ward);

    @Query(value = """
        SELECT COALESCE(SUM(pr.rated_power), 0)
        FROM power_rating pr
        JOIN transformer t ON pr.transformer_id = t.id
        JOIN asset_info ai ON t.asset_info_id = ai.id
        JOIN bay b ON ai.owner_id = b.id AND b.owner_type = 'VOLTAGE_LEVEL'
        JOIN voltage_level vl ON b.owner_id = vl.id
        LEFT JOIN substation s ON vl.owner_id = s.id
        LEFT JOIN address_detail ad ON s.address_detail_id = ad.id
        WHERE pr.status = 2 AND t.status = 2 AND ai.status = 2 AND vl.status = 2
          AND (:organisationId IS NULL OR s.owner_id = :organisationId)
          AND (:manufacturerId IS NULL OR ai.manufacturer_id = :manufacturerId)
          AND (:voltageLevel IS NULL OR vl.base_voltage = :voltageLevel)
          AND (:status IS NULL OR t.status = :status)
          AND (:city IS NULL OR ad.city = :city)
          AND (:ward IS NULL OR ad.ward_commune = :ward)
        """, nativeQuery = true)
    double sumRatedPowerFiltered(@Param("organisationId") Long organisationId,
                                  @Param("manufacturerId") Long manufacturerId,
                                  @Param("voltageLevel") Integer voltageLevel,
                                  @Param("status") Long status,
                                  @Param("city") String city,
                                  @Param("ward") String ward);

    @Query(value = """
        SELECT t.id, t.vector_group, t.base_voltage, t.base_power,
               sci.impedance, sci.impedance_unit,
               ai.asset_name, ai.serial_no,
               s.name as substation_name
        FROM transformer t
        JOIN asset_info ai ON t.asset_info_id = ai.id
        JOIN bay b ON ai.owner_id = b.id AND b.owner_type = 'VOLTAGE_LEVEL'
        JOIN voltage_level vl ON b.owner_id = vl.id
        LEFT JOIN short_circuit_impedance sci ON sci.transformer_id = t.id AND sci.status = 2
        LEFT JOIN substation s ON vl.owner_id = s.id
        LEFT JOIN address_detail ad ON s.address_detail_id = ad.id
        WHERE t.status = 2 AND ai.status = 2 AND vl.status = 2
          AND (:organisationId IS NULL OR s.owner_id = :organisationId)
          AND (:manufacturerId IS NULL OR ai.manufacturer_id = :manufacturerId)
          AND (:voltageLevel IS NULL OR vl.base_voltage = :voltageLevel)
          AND (:status IS NULL OR t.status = :status)
          AND (:city IS NULL OR ad.city = :city)
          AND (:ward IS NULL OR ad.ward_commune = :ward)
        """, nativeQuery = true)
    List<Object[]> findTransformerDetailsForCompatibility(@Param("organisationId") Long organisationId,
                                                           @Param("manufacturerId") Long manufacturerId,
                                                           @Param("voltageLevel") Integer voltageLevel,
                                                           @Param("status") Long status,
                                                           @Param("city") String city,
                                                           @Param("ward") String ward);

    @Query(value = """
        SELECT COALESCE(NULLIF(TRIM(o.short_name), ''), o.name, 'Unknown') as org_name,
               COUNT(DISTINCT t.id) as cnt
        FROM transformer t
        JOIN asset_info ai ON t.asset_info_id = ai.id
        JOIN bay b ON ai.owner_id = b.id AND b.owner_type = 'VOLTAGE_LEVEL'
        JOIN voltage_level vl ON b.owner_id = vl.id
        LEFT JOIN substation s ON vl.owner_id = s.id
        LEFT JOIN organisation o ON s.owner_id = o.id
        LEFT JOIN address_detail ad ON s.address_detail_id = ad.id
        WHERE t.status = 2 AND ai.status = 2 AND vl.status = 2
          AND (:organisationId IS NULL OR s.owner_id = :organisationId)
          AND (:manufacturerId IS NULL OR ai.manufacturer_id = :manufacturerId)
          AND (:voltageLevel IS NULL OR vl.base_voltage = :voltageLevel)
          AND (:status IS NULL OR t.status = :status)
          AND (:city IS NULL OR ad.city = :city)
          AND (:ward IS NULL OR ad.ward_commune = :ward)
        GROUP BY o.short_name, o.name
        ORDER BY cnt DESC
        """, nativeQuery = true)
    List<Object[]> countByOrganisation(@Param("organisationId") Long organisationId,
                                        @Param("manufacturerId") Long manufacturerId,
                                        @Param("voltageLevel") Integer voltageLevel,
                                        @Param("status") Long status,
                                        @Param("city") String city,
                                        @Param("ward") String ward);

    @Query(value = """
        SELECT COUNT(*) FROM (
            SELECT DISTINCT t.substation_owner_type
            FROM transformer t
            WHERE t.status = 2
        ) subq
        """, nativeQuery = true)
    long countDistinctSubstationTypes();
}
