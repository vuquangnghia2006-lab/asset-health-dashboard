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
        """, nativeQuery = true)
    long countActiveFiltered(@Param("organisationId") Long organisationId,
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
        WHERE a.status = 2 AND (a.serial_no IS NULL OR a.serial_no = '') AND vl.status = 2
          AND (:organisationId IS NULL OR s.owner_id = :organisationId)
          AND (:manufacturerId IS NULL OR a.manufacturer_id = :manufacturerId)
          AND (:voltageLevel IS NULL OR vl.base_voltage = :voltageLevel)
          AND (:city IS NULL OR ad.city = :city)
          AND (:ward IS NULL OR ad.ward_commune = :ward)
        """, nativeQuery = true)
    long countMissingSerialNoFiltered(@Param("organisationId") Long organisationId,
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
        """, nativeQuery = true)
    long countMissingManufacturerFiltered(@Param("organisationId") Long organisationId,
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
        WHERE a.status = 2 AND a.manufacturing_year IS NULL AND vl.status = 2
          AND (:organisationId IS NULL OR s.owner_id = :organisationId)
          AND (:manufacturerId IS NULL OR a.manufacturer_id = :manufacturerId)
          AND (:voltageLevel IS NULL OR vl.base_voltage = :voltageLevel)
          AND (:city IS NULL OR ad.city = :city)
          AND (:ward IS NULL OR ad.ward_commune = :ward)
        """, nativeQuery = true)
    long countMissingManufacturingYearFiltered(@Param("organisationId") Long organisationId,
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
        WHERE a.status = 2 AND a.country_of_origin IS NULL AND vl.status = 2
          AND (:organisationId IS NULL OR s.owner_id = :organisationId)
          AND (:manufacturerId IS NULL OR a.manufacturer_id = :manufacturerId)
          AND (:voltageLevel IS NULL OR vl.base_voltage = :voltageLevel)
          AND (:city IS NULL OR ad.city = :city)
          AND (:ward IS NULL OR ad.ward_commune = :ward)
        """, nativeQuery = true)
    long countMissingCountryOfOriginFiltered(@Param("organisationId") Long organisationId,
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
        SELECT COALESCE(m.name, 'Unknown') as field_name, COUNT(*) as cnt
        FROM asset_info a
        LEFT JOIN manufacturer m ON a.manufacturer_id = m.id
        WHERE a.status = 2
        GROUP BY m.name
        ORDER BY cnt DESC
        """, nativeQuery = true)
    List<Object[]> countByManufacturerName();
}
