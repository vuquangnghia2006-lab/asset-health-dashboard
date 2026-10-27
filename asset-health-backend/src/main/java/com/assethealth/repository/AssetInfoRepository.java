package com.assethealth.repository;

import com.assethealth.entity.AssetInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetInfoRepository extends JpaRepository<AssetInfo, Long> {

    @Query("SELECT COUNT(a) FROM AssetInfo a WHERE a.status = 2")
    long countActive();

    @Query("SELECT COUNT(a) FROM AssetInfo a")
    long countAll();

    @Query(value = """
        SELECT COUNT(a) FROM asset_info a
        JOIN bay b ON a.owner_id = b.id AND b.owner_type = 'VOLTAGE_LEVEL'
        JOIN voltage_level vl ON b.owner_id = vl.id
        LEFT JOIN substation s ON vl.owner_id = s.id
        LEFT JOIN address_detail ad ON s.address_detail_id = ad.id
        WHERE a.status = 2 AND vl.status = 2
          AND (:organisationId IS NULL OR s.owner_id = :organisationId)
          AND (:manufacturerId IS NULL OR a.manufacturer_id = :manufacturerId)
          AND (:voltageLevel IS NULL OR vl.base_voltage = :voltageLevel)
          AND (:city IS NULL OR ad.city = :city)
          AND (:ward IS NULL OR ad.ward_commune = :ward)
          AND (:assetClass IS NULL OR :assetClass = '' OR :assetClass = 'all'
            OR (:assetClass = 'transformer' AND EXISTS (
                SELECT 1 FROM transformer t2 WHERE t2.asset_info_id = a.id AND t2.status = 2))
            OR (:assetClass = 'circuit-breaker' AND EXISTS (
                SELECT 1 FROM circuit_breaker cb WHERE cb.asset_info_id = a.id AND cb.status = 2))
            OR (:assetClass = 'ct' AND EXISTS (
                SELECT 1 FROM current_transformer ct WHERE ct.asset_info_id = a.id AND ct.status = 2))
            OR (:assetClass = 'vt' AND EXISTS (
                SELECT 1 FROM voltage_transformer vt WHERE vt.asset_info_id = a.id AND vt.status = 2))
            OR (:assetClass = 'disconnector' AND EXISTS (
                SELECT 1 FROM disconnector d WHERE d.asset_info_id = a.id AND d.status = 2)))
        """, nativeQuery = true)
    long countActiveFiltered(@Param("organisationId") Long organisationId,
                              @Param("manufacturerId") Long manufacturerId,
                              @Param("voltageLevel") Integer voltageLevel,
                              @Param("city") String city,
                              @Param("ward") String ward,
                              @Param("assetClass") String assetClass);

    @Query(value = """
        SELECT COUNT(a) FROM asset_info a
        JOIN bay b ON a.owner_id = b.id AND b.owner_type = 'VOLTAGE_LEVEL'
        JOIN voltage_level vl ON b.owner_id = vl.id
        LEFT JOIN substation s ON vl.owner_id = s.id
        LEFT JOIN address_detail ad ON s.address_detail_id = ad.id
        WHERE a.status = 2 AND (a.serial_no IS NULL OR a.serial_no = '') AND vl.status = 2
          AND (:organisationId IS NULL OR s.owner_id = :organisationId)
          AND (:manufacturerId IS NULL OR a.manufacturer_id = :manufacturerId)
          AND (:voltageLevel IS NULL OR vl.base_voltage = :voltageLevel)
          AND (:city IS NULL OR ad.city = :city)
          AND (:ward IS NULL OR ad.ward_commune = :ward)
          AND (:assetClass IS NULL OR :assetClass = '' OR :assetClass = 'all'
            OR (:assetClass = 'transformer' AND EXISTS (
                SELECT 1 FROM transformer t2 WHERE t2.asset_info_id = a.id AND t2.status = 2))
            OR (:assetClass = 'circuit-breaker' AND EXISTS (
                SELECT 1 FROM circuit_breaker cb WHERE cb.asset_info_id = a.id AND cb.status = 2))
            OR (:assetClass = 'ct' AND EXISTS (
                SELECT 1 FROM current_transformer ct WHERE ct.asset_info_id = a.id AND ct.status = 2))
            OR (:assetClass = 'vt' AND EXISTS (
                SELECT 1 FROM voltage_transformer vt WHERE vt.asset_info_id = a.id AND vt.status = 2))
            OR (:assetClass = 'disconnector' AND EXISTS (
                SELECT 1 FROM disconnector d WHERE d.asset_info_id = a.id AND d.status = 2)))
        """, nativeQuery = true)
    long countMissingSerialNoFiltered(@Param("organisationId") Long organisationId,
                                       @Param("manufacturerId") Long manufacturerId,
                                       @Param("voltageLevel") Integer voltageLevel,
                                       @Param("city") String city,
                                       @Param("ward") String ward,
                                       @Param("assetClass") String assetClass);

    @Query(value = """
        SELECT COUNT(a) FROM asset_info a
        JOIN bay b ON a.owner_id = b.id AND b.owner_type = 'VOLTAGE_LEVEL'
        JOIN voltage_level vl ON b.owner_id = vl.id
        LEFT JOIN substation s ON vl.owner_id = s.id
        LEFT JOIN address_detail ad ON s.address_detail_id = ad.id
        WHERE a.status = 2 AND a.operation_date IS NULL AND vl.status = 2
          AND (:organisationId IS NULL OR s.owner_id = :organisationId)
          AND (:manufacturerId IS NULL OR a.manufacturer_id = :manufacturerId)
          AND (:voltageLevel IS NULL OR vl.base_voltage = :voltageLevel)
          AND (:city IS NULL OR ad.city = :city)
          AND (:ward IS NULL OR ad.ward_commune = :ward)
        """, nativeQuery = true)
    long countMissingOperationDateFiltered(@Param("organisationId") Long organisationId,
                                            @Param("manufacturerId") Long manufacturerId,
                                            @Param("voltageLevel") Integer voltageLevel,
                                            @Param("city") String city,
                                            @Param("ward") String ward);

    @Query(value = """
        SELECT COUNT(a) FROM asset_info a
        JOIN bay b ON a.owner_id = b.id AND b.owner_type = 'VOLTAGE_LEVEL'
        JOIN voltage_level vl ON b.owner_id = vl.id
        LEFT JOIN substation s ON vl.owner_id = s.id
        LEFT JOIN address_detail ad ON s.address_detail_id = ad.id
        WHERE a.status = 2 AND a.manufacturer_id IS NULL AND vl.status = 2
          AND (:organisationId IS NULL OR s.owner_id = :organisationId)
          AND (:manufacturerId IS NULL OR a.manufacturer_id = :manufacturerId)
          AND (:voltageLevel IS NULL OR vl.base_voltage = :voltageLevel)
          AND (:city IS NULL OR ad.city = :city)
          AND (:ward IS NULL OR ad.ward_commune = :ward)
          AND (:assetClass IS NULL OR :assetClass = '' OR :assetClass = 'all'
            OR (:assetClass = 'transformer' AND EXISTS (
                SELECT 1 FROM transformer t2 WHERE t2.asset_info_id = a.id AND t2.status = 2))
            OR (:assetClass = 'circuit-breaker' AND EXISTS (
                SELECT 1 FROM circuit_breaker cb WHERE cb.asset_info_id = a.id AND cb.status = 2))
            OR (:assetClass = 'ct' AND EXISTS (
                SELECT 1 FROM current_transformer ct WHERE ct.asset_info_id = a.id AND ct.status = 2))
            OR (:assetClass = 'vt' AND EXISTS (
                SELECT 1 FROM voltage_transformer vt WHERE vt.asset_info_id = a.id AND vt.status = 2))
            OR (:assetClass = 'disconnector' AND EXISTS (
                SELECT 1 FROM disconnector d WHERE d.asset_info_id = a.id AND d.status = 2)))
        """, nativeQuery = true)
    long countMissingManufacturerFiltered(@Param("organisationId") Long organisationId,
                                           @Param("manufacturerId") Long manufacturerId,
                                           @Param("voltageLevel") Integer voltageLevel,
                                           @Param("city") String city,
                                           @Param("ward") String ward,
                                           @Param("assetClass") String assetClass);

    @Query(value = """
        SELECT COUNT(a) FROM asset_info a
        JOIN bay b ON a.owner_id = b.id AND b.owner_type = 'VOLTAGE_LEVEL'
        JOIN voltage_level vl ON b.owner_id = vl.id
        LEFT JOIN substation s ON vl.owner_id = s.id
        LEFT JOIN address_detail ad ON s.address_detail_id = ad.id
        WHERE a.status = 2 AND a.manufacturing_year IS NULL AND vl.status = 2
          AND (:organisationId IS NULL OR s.owner_id = :organisationId)
          AND (:manufacturerId IS NULL OR a.manufacturer_id = :manufacturerId)
          AND (:voltageLevel IS NULL OR vl.base_voltage = :voltageLevel)
          AND (:city IS NULL OR ad.city = :city)
          AND (:ward IS NULL OR ad.ward_commune = :ward)
          AND (:assetClass IS NULL OR :assetClass = '' OR :assetClass = 'all'
            OR (:assetClass = 'transformer' AND EXISTS (
                SELECT 1 FROM transformer t2 WHERE t2.asset_info_id = a.id AND t2.status = 2))
            OR (:assetClass = 'circuit-breaker' AND EXISTS (
                SELECT 1 FROM circuit_breaker cb WHERE cb.asset_info_id = a.id AND cb.status = 2))
            OR (:assetClass = 'ct' AND EXISTS (
                SELECT 1 FROM current_transformer ct WHERE ct.asset_info_id = a.id AND ct.status = 2))
            OR (:assetClass = 'vt' AND EXISTS (
                SELECT 1 FROM voltage_transformer vt WHERE vt.asset_info_id = a.id AND vt.status = 2))
            OR (:assetClass = 'disconnector' AND EXISTS (
                SELECT 1 FROM disconnector d WHERE d.asset_info_id = a.id AND d.status = 2)))
        """, nativeQuery = true)
    long countMissingManufacturingYearFiltered(@Param("organisationId") Long organisationId,
                                                @Param("manufacturerId") Long manufacturerId,
                                                @Param("voltageLevel") Integer voltageLevel,
                                                @Param("city") String city,
                                                @Param("ward") String ward,
                                                @Param("assetClass") String assetClass);

    @Query(value = """
        SELECT COUNT(a) FROM asset_info a
        JOIN bay b ON a.owner_id = b.id AND b.owner_type = 'VOLTAGE_LEVEL'
        JOIN voltage_level vl ON b.owner_id = vl.id
        LEFT JOIN substation s ON vl.owner_id = s.id
        LEFT JOIN address_detail ad ON s.address_detail_id = ad.id
        WHERE a.status = 2 AND a.country_of_origin IS NULL AND vl.status = 2
          AND (:organisationId IS NULL OR s.owner_id = :organisationId)
          AND (:manufacturerId IS NULL OR a.manufacturer_id = :manufacturerId)
          AND (:voltageLevel IS NULL OR vl.base_voltage = :voltageLevel)
          AND (:city IS NULL OR ad.city = :city)
          AND (:ward IS NULL OR ad.ward_commune = :ward)
          AND (:assetClass IS NULL OR :assetClass = '' OR :assetClass = 'all'
            OR (:assetClass = 'transformer' AND EXISTS (
                SELECT 1 FROM transformer t2 WHERE t2.asset_info_id = a.id AND t2.status = 2))
            OR (:assetClass = 'circuit-breaker' AND EXISTS (
                SELECT 1 FROM circuit_breaker cb WHERE cb.asset_info_id = a.id AND cb.status = 2))
            OR (:assetClass = 'ct' AND EXISTS (
                SELECT 1 FROM current_transformer ct WHERE ct.asset_info_id = a.id AND ct.status = 2))
            OR (:assetClass = 'vt' AND EXISTS (
                SELECT 1 FROM voltage_transformer vt WHERE vt.asset_info_id = a.id AND vt.status = 2))
            OR (:assetClass = 'disconnector' AND EXISTS (
                SELECT 1 FROM disconnector d WHERE d.asset_info_id = a.id AND d.status = 2)))
        """, nativeQuery = true)
    long countMissingCountryOfOriginFiltered(@Param("organisationId") Long organisationId,
                                              @Param("manufacturerId") Long manufacturerId,
                                              @Param("voltageLevel") Integer voltageLevel,
                                              @Param("city") String city,
                                              @Param("ward") String ward,
                                              @Param("assetClass") String assetClass);

    @Query(value = """
        SELECT COUNT(a) FROM asset_info a
        JOIN bay b ON a.owner_id = b.id AND b.owner_type = 'VOLTAGE_LEVEL'
        JOIN voltage_level vl ON b.owner_id = vl.id
        LEFT JOIN substation s ON vl.owner_id = s.id
        LEFT JOIN address_detail ad ON s.address_detail_id = ad.id
        WHERE a.status = 2 AND a.serial_no IS NOT NULL AND a.serial_no <> ''
          AND a.manufacturer_id IS NOT NULL AND a.operation_date IS NOT NULL AND a.manufacturing_year IS NOT NULL
          AND vl.status = 2
          AND (:organisationId IS NULL OR s.owner_id = :organisationId)
          AND (:manufacturerId IS NULL OR a.manufacturer_id = :manufacturerId)
          AND (:voltageLevel IS NULL OR vl.base_voltage = :voltageLevel)
          AND (:city IS NULL OR ad.city = :city)
          AND (:ward IS NULL OR ad.ward_commune = :ward)
        """, nativeQuery = true)
    long countFullyCompleteFiltered(@Param("organisationId") Long organisationId,
                                     @Param("manufacturerId") Long manufacturerId,
                                     @Param("voltageLevel") Integer voltageLevel,
                                     @Param("city") String city,
                                     @Param("ward") String ward);

    @Query(value = """
        SELECT COUNT(a) FROM asset_info a
        JOIN bay b ON a.owner_id = b.id AND b.owner_type = 'VOLTAGE_LEVEL'
        JOIN voltage_level vl ON b.owner_id = vl.id
        LEFT JOIN substation s ON vl.owner_id = s.id
        LEFT JOIN address_detail ad ON s.address_detail_id = ad.id
        WHERE a.status = 2 AND (a.serial_no IS NULL OR a.serial_no = ''
          OR a.manufacturer_id IS NULL OR a.operation_date IS NULL)
          AND vl.status = 2
          AND (:organisationId IS NULL OR s.owner_id = :organisationId)
          AND (:manufacturerId IS NULL OR a.manufacturer_id = :manufacturerId)
          AND (:voltageLevel IS NULL OR vl.base_voltage = :voltageLevel)
          AND (:city IS NULL OR ad.city = :city)
          AND (:ward IS NULL OR ad.ward_commune = :ward)
        """, nativeQuery = true)
    long countIncompleteFiltered(@Param("organisationId") Long organisationId,
                                  @Param("manufacturerId") Long manufacturerId,
                                  @Param("voltageLevel") Integer voltageLevel,
                                  @Param("city") String city,
                                  @Param("ward") String ward);

    @Query("SELECT COUNT(a) FROM AssetInfo a WHERE a.serialNo IS NULL OR a.serialNo = ''")
    long countMissingSerialNo();

    @Query("SELECT COUNT(a) FROM AssetInfo a WHERE a.operationDate IS NULL")
    long countMissingOperationDate();

    @Query("SELECT COUNT(a) FROM AssetInfo a WHERE a.manufacturerId IS NULL")
    long countMissingManufacturer();

    @Query("SELECT COUNT(a) FROM AssetInfo a WHERE a.manufacturingYear IS NULL")
    long countMissingManufacturingYear();

    @Query("SELECT COUNT(a) FROM AssetInfo a WHERE a.countryOfOrigin IS NULL")
    long countMissingCountryOfOrigin();

    @Query("SELECT COUNT(a) FROM AssetInfo a WHERE a.status = 2 AND a.serialNo IS NOT NULL AND a.serialNo <> '' AND a.manufacturerId IS NOT NULL AND a.operationDate IS NOT NULL")
    long countComplete();

    @Query("SELECT COUNT(a) FROM AssetInfo a WHERE a.status = 2 AND (a.serialNo IS NULL OR a.serialNo = '' OR a.manufacturerId IS NULL OR a.operationDate IS NULL)")
    long countIncomplete();

    @Query("SELECT COUNT(a) FROM AssetInfo a WHERE a.status = 2 AND a.serialNo IS NOT NULL AND a.serialNo <> '' AND a.manufacturerId IS NOT NULL AND a.operationDate IS NOT NULL AND a.manufacturingYear IS NOT NULL")
    long countFullyComplete();

    @Query("SELECT COUNT(a) FROM AssetInfo a WHERE a.phase IS NOT NULL AND a.phase <> ''")
    long countWithPhase();

    @Query(value = "SELECT a.phase, COUNT(*) FROM asset_info a WHERE a.phase IS NOT NULL AND a.phase <> '' AND a.status = 2 GROUP BY a.phase ORDER BY a.phase", nativeQuery = true)
    List<Object[]> countByPhase();

    @Query(value = """
        WITH filtered_assets AS (
            SELECT
                (a.serial_no IS NULL OR a.serial_no = '') AS missing_serial,
                (a.asset_name IS NULL OR a.asset_name = '') AS missing_asset_name,
                (a.manufacturer_id IS NULL) AS missing_manufacturer,
                (a.operation_date IS NULL) AS missing_operation_date,
                (a.manufacturing_year IS NULL) AS missing_manufacturing_year,
                (a.country_of_origin IS NULL) AS missing_country,
                (a.appartus_id IS NULL OR a.appartus_id = '') AS missing_apparatus,
                CASE
                    WHEN (a.serial_no IS NULL OR a.serial_no = '')
                        OR (a.manufacturer_id IS NULL)
                        OR (a.operation_date IS NULL)
                        OR (a.manufacturing_year IS NULL) THEN 'incomplete_or_invalid'
                    ELSE 'complete'
                END AS complete_marker,
                CASE
                    WHEN (a.serial_no IS NULL OR a.serial_no = '') THEN 'invalid'
                    WHEN (a.manufacturer_id IS NULL)
                        OR (a.operation_date IS NULL)
                        OR (a.manufacturing_year IS NULL) THEN 'incomplete'
                    ELSE 'complete'
                END AS quality_status
            FROM asset_info a
            JOIN bay b ON a.owner_id = b.id AND b.owner_type = 'VOLTAGE_LEVEL' AND b.status = 2
            JOIN voltage_level vl ON vl.id = b.owner_id AND vl.status = 2
            LEFT JOIN substation s ON s.id = vl.owner_id
            LEFT JOIN address_detail ad ON ad.id = s.address_detail_id
            WHERE a.status = 2 AND a.owner_type = 'BAY'
              AND (:organisationId IS NULL OR s.owner_id = :organisationId)
              AND (:manufacturerId IS NULL OR a.manufacturer_id = :manufacturerId)
              AND (:voltageLevel IS NULL OR vl.base_voltage = :voltageLevel)
              AND (:city IS NULL OR ad.city = :city)
              AND (:assetClass IS NULL OR :assetClass = '' OR :assetClass = 'all'
                OR (:assetClass = 'transformer' AND EXISTS (
                    SELECT 1 FROM transformer t WHERE t.asset_info_id = a.id AND t.status = 2))
                OR (:assetClass = 'circuit-breaker' AND EXISTS (
                    SELECT 1 FROM circuit_breaker cb WHERE cb.asset_info_id = a.id AND cb.status = 2))
                OR (:assetClass = 'ct' AND EXISTS (
                    SELECT 1 FROM current_transformer ct WHERE ct.asset_info_id = a.id AND ct.status = 2))
                OR (:assetClass = 'vt' AND EXISTS (
                    SELECT 1 FROM voltage_transformer vt WHERE vt.asset_info_id = a.id AND vt.status = 2))
                OR (:assetClass = 'disconnector' AND EXISTS (
                    SELECT 1 FROM disconnector d WHERE d.asset_info_id = a.id AND d.status = 2)))
        )
        SELECT COUNT(*),
            SUM(CASE WHEN missing_serial THEN 1 ELSE 0 END),
            SUM(CASE WHEN missing_asset_name THEN 1 ELSE 0 END),
            SUM(CASE WHEN missing_manufacturer THEN 1 ELSE 0 END),
            SUM(CASE WHEN missing_operation_date THEN 1 ELSE 0 END),
            SUM(CASE WHEN missing_manufacturing_year THEN 1 ELSE 0 END),
            SUM(CASE WHEN missing_country THEN 1 ELSE 0 END),
            SUM(CASE WHEN missing_apparatus THEN 1 ELSE 0 END),
            SUM(CASE WHEN missing_serial OR missing_manufacturer OR missing_operation_date THEN 1 ELSE 0 END),
            SUM(CASE WHEN missing_manufacturer OR missing_manufacturing_year THEN 1 ELSE 0 END),
            SUM(CASE WHEN missing_serial OR missing_manufacturer OR missing_operation_date
                OR missing_asset_name OR missing_manufacturing_year OR missing_country
                OR missing_apparatus THEN 1 ELSE 0 END),
            SUM(CASE WHEN quality_status = 'complete' THEN 1 ELSE 0 END),
            SUM(CASE WHEN quality_status = 'invalid' THEN 1 ELSE 0 END),
            SUM(CASE WHEN quality_status = 'incomplete' THEN 1 ELSE 0 END)
        FROM filtered_assets
        WHERE (:qualityStatus IS NULL OR :qualityStatus = '' OR :qualityStatus = 'all'
            OR quality_status = :qualityStatus)
        """, nativeQuery = true)
    List<Object[]> findQualityFieldCounts(@Param("organisationId") Long organisationId,
                                          @Param("manufacturerId") Long manufacturerId,
                                          @Param("voltageLevel") Integer voltageLevel,
                                          @Param("city") String city,
                                          @Param("assetClass") String assetClass,
                                          @Param("qualityStatus") String qualityStatus);

    @Query(value = """
        WITH filtered_assets AS (
            SELECT a.id AS asset_id, b.id AS bay_id,
                CASE
                    WHEN (a.serial_no IS NULL OR a.serial_no = '') THEN 'invalid'
                    WHEN (a.manufacturer_id IS NULL)
                        OR (a.operation_date IS NULL)
                        OR (a.manufacturing_year IS NULL) THEN 'incomplete'
                    ELSE 'complete'
                END AS quality_status
            FROM asset_info a
            JOIN bay b ON a.owner_id = b.id AND b.owner_type = 'VOLTAGE_LEVEL' AND b.status = 2
            JOIN voltage_level vl ON vl.id = b.owner_id AND vl.status = 2
            LEFT JOIN substation s ON s.id = vl.owner_id
            LEFT JOIN address_detail ad ON ad.id = s.address_detail_id
            WHERE a.status = 2 AND a.owner_type = 'BAY'
              AND (:organisationId IS NULL OR s.owner_id = :organisationId)
              AND (:manufacturerId IS NULL OR a.manufacturer_id = :manufacturerId)
              AND (:voltageLevel IS NULL OR vl.base_voltage = :voltageLevel)
              AND (:city IS NULL OR ad.city = :city)
        ),
        records AS (
            SELECT 'Current Transformer' AS asset_class, fa.bay_id AS bay_id,
                UPPER(TRIM(x.phase)) AS phase, fa.quality_status AS quality_status
            FROM current_transformer x
            JOIN filtered_assets fa ON fa.asset_id = x.asset_info_id
            WHERE x.status = 2
              AND (:assetClass IS NULL OR :assetClass = '' OR :assetClass = 'all' OR :assetClass = 'ct')
            UNION ALL
            SELECT 'Voltage Transformer', fa.bay_id, UPPER(TRIM(x.phase)), fa.quality_status
            FROM voltage_transformer x
            JOIN filtered_assets fa ON fa.asset_id = x.asset_info_id
            WHERE x.status = 2
              AND (:assetClass IS NULL OR :assetClass = '' OR :assetClass = 'all' OR :assetClass = 'vt')
            UNION ALL
            SELECT 'Surge Arrester', fa.bay_id, UPPER(TRIM(fa_asset.phase)), fa.quality_status
            FROM surge_arrester x
            JOIN asset_info fa_asset ON fa_asset.id = x.asset_info_id
            JOIN filtered_assets fa ON fa.asset_id = x.asset_info_id
            WHERE x.status = 2
              AND (:assetClass IS NULL OR :assetClass = '' OR :assetClass = 'all')
            UNION ALL
            SELECT 'Bushing', fa.bay_id, UPPER(TRIM(fa_asset.phase)), fa.quality_status
            FROM bushing x
            JOIN asset_info fa_asset ON fa_asset.id = x.asset_info_id
            JOIN filtered_assets fa ON fa.asset_id = x.asset_info_id
            WHERE x.status = 2
              AND (:assetClass IS NULL OR :assetClass = '' OR :assetClass = 'all')
        )
        SELECT asset_class, bay_id,
            SUM(CASE WHEN phase = 'A' THEN 1 ELSE 0 END),
            SUM(CASE WHEN phase = 'B' THEN 1 ELSE 0 END),
            SUM(CASE WHEN phase = 'C' THEN 1 ELSE 0 END)
        FROM records
        WHERE phase IN ('A', 'B', 'C')
          AND (:qualityStatus IS NULL OR :qualityStatus = '' OR :qualityStatus = 'all'
            OR quality_status = :qualityStatus)
        GROUP BY asset_class, bay_id
        ORDER BY asset_class, bay_id
        """, nativeQuery = true)
    List<Object[]> findQualityPhaseGroups(@Param("organisationId") Long organisationId,
                                          @Param("manufacturerId") Long manufacturerId,
                                          @Param("voltageLevel") Integer voltageLevel,
                                          @Param("city") String city,
                                          @Param("assetClass") String assetClass,
                                          @Param("qualityStatus") String qualityStatus);

    @Query(value = """
        SELECT COALESCE(m.name, 'Unknown') as field_name, COUNT(*) as cnt
        FROM asset_info a
        LEFT JOIN manufacturer m ON a.manufacturer_id = m.id
        WHERE a.status = 2
        GROUP BY m.name
        ORDER BY cnt DESC
        """, nativeQuery = true)
    List<Object[]> countByManufacturerName();
}
