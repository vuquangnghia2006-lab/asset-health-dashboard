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
          AND (:substationId IS NULL OR s.id = :substationId)
          AND (:assetClass IS NULL OR :assetClass = '' OR :assetClass = 'all'
            OR (:assetClass = 'transformer' AND EXISTS (
                SELECT 1 FROM transformer t2 WHERE t2.asset_info_id = ai.id AND t2.status = 2))
            OR (:assetClass = 'circuit-breaker' AND EXISTS (
                SELECT 1 FROM circuit_breaker cb WHERE cb.asset_info_id = ai.id AND cb.status = 2))
            OR (:assetClass = 'ct' AND EXISTS (
                SELECT 1 FROM current_transformer ct WHERE ct.asset_info_id = ai.id AND ct.status = 2))
            OR (:assetClass = 'vt' AND EXISTS (
                SELECT 1 FROM voltage_transformer vt WHERE vt.asset_info_id = ai.id AND vt.status = 2))
            OR (:assetClass = 'disconnector' AND EXISTS (
                SELECT 1 FROM disconnector d WHERE d.asset_info_id = ai.id AND d.status = 2)))
        GROUP BY vl.base_voltage
        ORDER BY kv DESC
        """, nativeQuery = true)
    List<Object[]> capacityByVoltageLevel(@Param("organisationId") Long organisationId,
                                           @Param("manufacturerId") Long manufacturerId,
                                           @Param("voltageLevel") Integer voltageLevel,
                                           @Param("status") Long status,
                                           @Param("city") String city,
                                           @Param("ward") String ward,
                           @Param("substationId") Long substationId,
                           @Param("assetClass") String assetClass);

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
          AND (:substationId IS NULL OR s.id = :substationId)
          AND (:assetClass IS NULL OR :assetClass = '' OR :assetClass = 'all'
            OR (:assetClass = 'transformer' AND EXISTS (
                SELECT 1 FROM transformer t2 WHERE t2.asset_info_id = ai.id AND t2.status = 2))
            OR (:assetClass = 'circuit-breaker' AND EXISTS (
                SELECT 1 FROM circuit_breaker cb WHERE cb.asset_info_id = ai.id AND cb.status = 2))
            OR (:assetClass = 'ct' AND EXISTS (
                SELECT 1 FROM current_transformer ct WHERE ct.asset_info_id = ai.id AND ct.status = 2))
            OR (:assetClass = 'vt' AND EXISTS (
                SELECT 1 FROM voltage_transformer vt WHERE vt.asset_info_id = ai.id AND vt.status = 2))
            OR (:assetClass = 'disconnector' AND EXISTS (
                SELECT 1 FROM disconnector d WHERE d.asset_info_id = ai.id AND d.status = 2)))
        GROUP BY m.name
        ORDER BY cnt DESC
        """, nativeQuery = true)
    List<Object[]> countByManufacturer(@Param("organisationId") Long organisationId,
                                        @Param("manufacturerId") Long manufacturerId,
                                        @Param("voltageLevel") Integer voltageLevel,
                                        @Param("status") Long status,
                                        @Param("city") String city,
                                        @Param("ward") String ward,
                           @Param("substationId") Long substationId,
                           @Param("assetClass") String assetClass);

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
          AND (:substationId IS NULL OR s.id = :substationId)
          AND (:assetClass IS NULL OR :assetClass = '' OR :assetClass = 'all'
            OR (:assetClass = 'transformer' AND EXISTS (
                SELECT 1 FROM transformer t2 WHERE t2.asset_info_id = ai.id AND t2.status = 2))
            OR (:assetClass = 'circuit-breaker' AND EXISTS (
                SELECT 1 FROM circuit_breaker cb WHERE cb.asset_info_id = ai.id AND cb.status = 2))
            OR (:assetClass = 'ct' AND EXISTS (
                SELECT 1 FROM current_transformer ct WHERE ct.asset_info_id = ai.id AND ct.status = 2))
            OR (:assetClass = 'vt' AND EXISTS (
                SELECT 1 FROM voltage_transformer vt WHERE vt.asset_info_id = ai.id AND vt.status = 2))
            OR (:assetClass = 'disconnector' AND EXISTS (
                SELECT 1 FROM disconnector d WHERE d.asset_info_id = ai.id AND d.status = 2)))
        """, nativeQuery = true)
    long countActiveFiltered(@Param("organisationId") Long organisationId,
                              @Param("manufacturerId") Long manufacturerId,
                              @Param("voltageLevel") Integer voltageLevel,
                              @Param("status") Long status,
                              @Param("city") String city,
                              @Param("ward") String ward,
                           @Param("substationId") Long substationId,
                           @Param("assetClass") String assetClass);

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
          AND (:substationId IS NULL OR s.id = :substationId)
          AND (:assetClass IS NULL OR :assetClass = '' OR :assetClass = 'all'
            OR (:assetClass = 'transformer' AND EXISTS (
                SELECT 1 FROM transformer t2 WHERE t2.asset_info_id = ai.id AND t2.status = 2))
            OR (:assetClass = 'circuit-breaker' AND EXISTS (
                SELECT 1 FROM circuit_breaker cb WHERE cb.asset_info_id = ai.id AND cb.status = 2))
            OR (:assetClass = 'ct' AND EXISTS (
                SELECT 1 FROM current_transformer ct WHERE ct.asset_info_id = ai.id AND ct.status = 2))
            OR (:assetClass = 'vt' AND EXISTS (
                SELECT 1 FROM voltage_transformer vt WHERE vt.asset_info_id = ai.id AND vt.status = 2))
            OR (:assetClass = 'disconnector' AND EXISTS (
                SELECT 1 FROM disconnector d WHERE d.asset_info_id = ai.id AND d.status = 2)))
        """, nativeQuery = true)
    double sumRatedPowerFiltered(@Param("organisationId") Long organisationId,
                                  @Param("manufacturerId") Long manufacturerId,
                                  @Param("voltageLevel") Integer voltageLevel,
                                  @Param("status") Long status,
                                  @Param("city") String city,
                                  @Param("ward") String ward,
                           @Param("substationId") Long substationId,
                           @Param("assetClass") String assetClass);

    @Query(value = """
        SELECT COUNT(DISTINCT t.id)
        FROM transformer t
        JOIN asset_info ai ON t.asset_info_id = ai.id
        JOIN bay b ON ai.owner_id = b.id AND b.owner_type = 'VOLTAGE_LEVEL'
        JOIN voltage_level vl ON b.owner_id = vl.id
        LEFT JOIN substation s ON vl.owner_id = s.id
        LEFT JOIN address_detail ad ON s.address_detail_id = ad.id
        WHERE t.status = 2 AND ai.status = 2 AND vl.status = 2
          AND EXISTS (SELECT 1 FROM power_rating pr2 WHERE pr2.transformer_id = t.id AND pr2.status = 2)
          AND (:organisationId IS NULL OR s.owner_id = :organisationId)
          AND (:manufacturerId IS NULL OR ai.manufacturer_id = :manufacturerId)
          AND (:voltageLevel IS NULL OR vl.base_voltage = :voltageLevel)
          AND (:status IS NULL OR t.status = :status)
          AND (:city IS NULL OR ad.city = :city)
          AND (:ward IS NULL OR ad.ward_commune = :ward)
          AND (:substationId IS NULL OR s.id = :substationId)
          AND (:assetClass IS NULL OR :assetClass = '' OR :assetClass = 'all'
            OR (:assetClass = 'transformer' AND EXISTS (
                SELECT 1 FROM transformer t2 WHERE t2.asset_info_id = ai.id AND t2.status = 2))
            OR (:assetClass = 'circuit-breaker' AND EXISTS (
                SELECT 1 FROM circuit_breaker cb WHERE cb.asset_info_id = ai.id AND cb.status = 2))
            OR (:assetClass = 'ct' AND EXISTS (
                SELECT 1 FROM current_transformer ct WHERE ct.asset_info_id = ai.id AND ct.status = 2))
            OR (:assetClass = 'vt' AND EXISTS (
                SELECT 1 FROM voltage_transformer vt WHERE vt.asset_info_id = ai.id AND vt.status = 2))
            OR (:assetClass = 'disconnector' AND EXISTS (
                SELECT 1 FROM disconnector d WHERE d.asset_info_id = ai.id AND d.status = 2)))
        """, nativeQuery = true)
    long countWithRatedPowerFiltered(@Param("organisationId") Long organisationId,
                                     @Param("manufacturerId") Long manufacturerId,
                                     @Param("voltageLevel") Integer voltageLevel,
                                     @Param("status") Long status,
                                     @Param("city") String city,
                                     @Param("ward") String ward,
                           @Param("substationId") Long substationId,
                           @Param("assetClass") String assetClass);

    @Query(value = """
        SELECT AVG(EXTRACT(YEAR FROM ai.operation_date))::numeric, COUNT(ai.operation_date)
        FROM transformer t
        JOIN asset_info ai ON t.asset_info_id = ai.id
        JOIN bay b ON ai.owner_id = b.id AND b.owner_type = 'VOLTAGE_LEVEL'
        JOIN voltage_level vl ON b.owner_id = vl.id
        LEFT JOIN substation s ON vl.owner_id = s.id
        LEFT JOIN address_detail ad ON s.address_detail_id = ad.id
        LEFT JOIN power_rating pr ON pr.transformer_id = t.id AND pr.status = 2
        WHERE t.status = 2 AND ai.status = 2 AND vl.status = 2 AND ai.operation_date IS NOT NULL
          AND ai.operation_date <= CURRENT_TIMESTAMP
          AND (:organisationId IS NULL OR s.owner_id = :organisationId)
          AND (:manufacturerId IS NULL OR ai.manufacturer_id = :manufacturerId)
          AND (:voltageLevel IS NULL OR vl.base_voltage = :voltageLevel)
          AND (:status IS NULL OR t.status = :status)
          AND (:city IS NULL OR ad.city = :city)
          AND (:ward IS NULL OR ad.ward_commune = :ward)
          AND (:substationId IS NULL OR s.id = :substationId)
          AND (:assetClass IS NULL OR :assetClass = '' OR :assetClass = 'all'
            OR (:assetClass = 'transformer' AND EXISTS (
                SELECT 1 FROM transformer t2 WHERE t2.asset_info_id = ai.id AND t2.status = 2))
            OR (:assetClass = 'circuit-breaker' AND EXISTS (
                SELECT 1 FROM circuit_breaker cb WHERE cb.asset_info_id = ai.id AND cb.status = 2))
            OR (:assetClass = 'ct' AND EXISTS (
                SELECT 1 FROM current_transformer ct WHERE ct.asset_info_id = ai.id AND ct.status = 2))
            OR (:assetClass = 'vt' AND EXISTS (
                SELECT 1 FROM voltage_transformer vt WHERE vt.asset_info_id = ai.id AND vt.status = 2))
            OR (:assetClass = 'disconnector' AND EXISTS (
                SELECT 1 FROM disconnector d WHERE d.asset_info_id = ai.id AND d.status = 2)))
        """, nativeQuery = true)
    List<Object[]> averageOperationDateYearFiltered(@Param("organisationId") Long organisationId,
                                                     @Param("manufacturerId") Long manufacturerId,
                                                     @Param("voltageLevel") Integer voltageLevel,
                                                     @Param("status") Long status,
                                                     @Param("city") String city,
                                                     @Param("ward") String ward,
                           @Param("substationId") Long substationId,
                           @Param("assetClass") String assetClass);

    @Query(value = """
        SELECT DISTINCT t.id, t.vector_group, t.base_voltage, t.base_power,
               ai.asset_name, ai.serial_no,
               s.id as substation_id, s.name as substation_name
        FROM transformer t
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
          AND (:substationId IS NULL OR s.id = :substationId)
          AND (:assetClass IS NULL OR :assetClass = '' OR :assetClass = 'all'
            OR (:assetClass = 'transformer' AND EXISTS (
                SELECT 1 FROM transformer t2 WHERE t2.asset_info_id = ai.id AND t2.status = 2))
            OR (:assetClass = 'circuit-breaker' AND EXISTS (
                SELECT 1 FROM circuit_breaker cb WHERE cb.asset_info_id = ai.id AND cb.status = 2))
            OR (:assetClass = 'ct' AND EXISTS (
                SELECT 1 FROM current_transformer ct WHERE ct.asset_info_id = ai.id AND ct.status = 2))
            OR (:assetClass = 'vt' AND EXISTS (
                SELECT 1 FROM voltage_transformer vt WHERE vt.asset_info_id = ai.id AND vt.status = 2))
            OR (:assetClass = 'disconnector' AND EXISTS (
                SELECT 1 FROM disconnector d WHERE d.asset_info_id = ai.id AND d.status = 2)))
        ORDER BY s.name, ai.asset_name, t.id
        LIMIT 200
        """, nativeQuery = true)
    List<Object[]> findTransformerDetailsForCompatibility(@Param("organisationId") Long organisationId,
                                                           @Param("manufacturerId") Long manufacturerId,
                                                           @Param("voltageLevel") Integer voltageLevel,
                                                           @Param("status") Long status,
                                                           @Param("city") String city,
                                                           @Param("ward") String ward,
                           @Param("substationId") Long substationId,
                           @Param("assetClass") String assetClass);

    @Query(value = """
        SELECT DISTINCT transformer_id, winding, voltage_l_l, voltage_l_l_unit
        FROM voltage_rating
        WHERE status = 2 AND transformer_id IN (:ids)
        ORDER BY transformer_id, winding
        """, nativeQuery = true)
    List<Object[]> findVoltageRatingsByTransformerIds(@Param("ids") List<Long> ids);

    @Query(value = """
        SELECT DISTINCT id, transformer_id, type, winding, tab_scheme, tap_number, no_tap, manufacturer_type
        FROM tap_changer
        WHERE status = 2 AND transformer_id IN (:ids)
        ORDER BY transformer_id, winding, type, id
        """, nativeQuery = true)
    List<Object[]> findTapChangersByTransformerIds(@Param("ids") List<Long> ids);

    @Query(value = """
        SELECT DISTINCT tap_changer_id, tap, voltage, voltage_unit
        FROM tap_changer_voltage
        WHERE status = 2 AND tap_changer_id IN (:ids)
        ORDER BY tap_changer_id, tap
        """, nativeQuery = true)
    List<Object[]> findTapChangerVoltagesByChangerIds(@Param("ids") List<Long> ids);

    @Query(value = """
        SELECT DISTINCT transformer_id, type, impedance, impedance_unit
        FROM short_circuit_impedance
        WHERE status = 2 AND transformer_id IN (:ids)
        ORDER BY transformer_id, type
        """, nativeQuery = true)
    List<Object[]> findImpedancesByTransformerIds(@Param("ids") List<Long> ids);

    @Query(value = """
        SELECT DISTINCT transformer_id, rated_power, rated_power_unit, cooling_class
        FROM power_rating
        WHERE status = 2 AND transformer_id IN (:ids)
        ORDER BY transformer_id
        """, nativeQuery = true)
    List<Object[]> findPowerRatingsByTransformerIds(@Param("ids") List<Long> ids);

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
          AND (:substationId IS NULL OR s.id = :substationId)
          AND (:assetClass IS NULL OR :assetClass = '' OR :assetClass = 'all'
            OR (:assetClass = 'transformer' AND EXISTS (
                SELECT 1 FROM transformer t2 WHERE t2.asset_info_id = ai.id AND t2.status = 2))
            OR (:assetClass = 'circuit-breaker' AND EXISTS (
                SELECT 1 FROM circuit_breaker cb WHERE cb.asset_info_id = ai.id AND cb.status = 2))
            OR (:assetClass = 'ct' AND EXISTS (
                SELECT 1 FROM current_transformer ct WHERE ct.asset_info_id = ai.id AND ct.status = 2))
            OR (:assetClass = 'vt' AND EXISTS (
                SELECT 1 FROM voltage_transformer vt WHERE vt.asset_info_id = ai.id AND vt.status = 2))
            OR (:assetClass = 'disconnector' AND EXISTS (
                SELECT 1 FROM disconnector d WHERE d.asset_info_id = ai.id AND d.status = 2)))
        GROUP BY o.short_name, o.name
        ORDER BY cnt DESC
        """, nativeQuery = true)
    List<Object[]> countByOrganisation(@Param("organisationId") Long organisationId,
                                        @Param("manufacturerId") Long manufacturerId,
                                        @Param("voltageLevel") Integer voltageLevel,
                                        @Param("status") Long status,
                                        @Param("city") String city,
                                        @Param("ward") String ward,
                           @Param("substationId") Long substationId,
                           @Param("assetClass") String assetClass);

    @Query(value = """
        SELECT COUNT(*) FROM (
            SELECT DISTINCT t.substation_owner_type
            FROM transformer t
            WHERE t.status = 2
        ) subq
        """, nativeQuery = true)
    long countDistinctSubstationTypes();

    @Query(value = """
        SELECT COUNT(DISTINCT s.id)
        FROM transformer t
        JOIN asset_info ai ON t.asset_info_id = ai.id
        JOIN bay b ON ai.owner_id = b.id AND b.owner_type = 'VOLTAGE_LEVEL'
        JOIN voltage_level vl ON b.owner_id = vl.id
        LEFT JOIN substation s ON vl.owner_id = s.id
        LEFT JOIN address_detail ad ON s.address_detail_id = ad.id
        WHERE t.status = 2 AND ai.status = 2 AND vl.status = 2 AND s.id IS NOT NULL
          AND (:organisationId IS NULL OR s.owner_id = :organisationId)
          AND (:manufacturerId IS NULL OR ai.manufacturer_id = :manufacturerId)
          AND (:voltageLevel IS NULL OR vl.base_voltage = :voltageLevel)
          AND (:status IS NULL OR t.status = :status)
          AND (:city IS NULL OR ad.city = :city)
          AND (:ward IS NULL OR ad.ward_commune = :ward)
          AND (:substationId IS NULL OR s.id = :substationId)
          AND (:assetClass IS NULL OR :assetClass = '' OR :assetClass = 'all'
            OR (:assetClass = 'transformer' AND EXISTS (
                SELECT 1 FROM transformer t2 WHERE t2.asset_info_id = ai.id AND t2.status = 2))
            OR (:assetClass = 'circuit-breaker' AND EXISTS (
                SELECT 1 FROM circuit_breaker cb WHERE cb.asset_info_id = ai.id AND cb.status = 2))
            OR (:assetClass = 'ct' AND EXISTS (
                SELECT 1 FROM current_transformer ct WHERE ct.asset_info_id = ai.id AND ct.status = 2))
            OR (:assetClass = 'vt' AND EXISTS (
                SELECT 1 FROM voltage_transformer vt WHERE vt.asset_info_id = ai.id AND vt.status = 2))
            OR (:assetClass = 'disconnector' AND EXISTS (
                SELECT 1 FROM disconnector d WHERE d.asset_info_id = ai.id AND d.status = 2)))
        """, nativeQuery = true)
    long countSubstationsFiltered(@Param("organisationId") Long organisationId,
                                  @Param("manufacturerId") Long manufacturerId,
                                  @Param("voltageLevel") Integer voltageLevel,
                                  @Param("status") Long status,
                                  @Param("city") String city,
                                  @Param("ward") String ward,
                                  @Param("substationId") Long substationId,
                                  @Param("assetClass") String assetClass);

}
