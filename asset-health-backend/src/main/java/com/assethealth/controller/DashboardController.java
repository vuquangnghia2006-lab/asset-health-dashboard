package com.assethealth.controller;

import com.assethealth.dto.*;
import com.assethealth.entity.*;
import com.assethealth.repository.*;
import com.assethealth.service.DashboardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;
    private final OrganisationRepository organisationRepo;
    private final SubstationRepository substationRepo;
    private final ManufacturerRepository manufacturerRepo;
    private final AddressDetailRepository addressDetailRepo;

    public DashboardController(DashboardService dashboardService, OrganisationRepository organisationRepo,
                               SubstationRepository substationRepo, ManufacturerRepository manufacturerRepo,
                               AddressDetailRepository addressDetailRepo) {
        this.dashboardService = dashboardService;
        this.organisationRepo = organisationRepo;
        this.substationRepo = substationRepo;
        this.manufacturerRepo = manufacturerRepo;
        this.addressDetailRepo = addressDetailRepo;
    }

    @GetMapping("/transformers/summary")
    public TransformerFleetSummary getTransformerSummary(
            @RequestParam(required = false) String organisation,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) String voltage,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String ward) {
        return dashboardService.getTransformerFleetSummary(organisation, manufacturer, voltage, status, city, ward);
    }

    @GetMapping("/transformers/capacity-by-voltage")
    public List<VoltageCapacityItem> getCapacityByVoltage(
            @RequestParam(required = false) String organisation,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) String voltage,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String ward) {
        return dashboardService.getCapacityByVoltage(organisation, manufacturer, voltage, status, city, ward);
    }

    @GetMapping("/transformers/mix-by-organisation")
    public List<AssetMixItem> getMixByOrganisation(
            @RequestParam(required = false) String organisation,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) String voltage,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String ward) {
        return dashboardService.getAssetMixByOrganisation(organisation, manufacturer, voltage, status, city, ward);
    }

    @GetMapping("/transformers/manufacturer-mix")
    public List<AssetMixItem> getManufacturerMix(
            @RequestParam(required = false) String organisation,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) String voltage,
            @RequestParam(required = false) String status) {
        return dashboardService.getAssetMixByOrganisation(organisation, manufacturer, voltage, status, null, null);
    }

    @GetMapping("/transformers/top-manufacturers")
    public List<ChartItem> getTopManufacturers(
            @RequestParam(required = false) String organisation,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) String voltage,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String ward) {
        return dashboardService.getTopManufacturers(organisation, manufacturer, voltage, status, city, ward);
    }

    @GetMapping("/transformers/data-completeness")
    public List<DataCompletenessItem> getDataCompleteness(
            @RequestParam(required = false) String organisation,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) String voltage,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String ward) {
        return dashboardService.getDataCompleteness(organisation, manufacturer, voltage, status, city, ward);
    }

    @GetMapping("/transformers/compatibility")
    public List<CompatibilityResult> getCompatibilityResults(
            @RequestParam(required = false) String organisation,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) String voltage,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean sameSiteOnly,
            @RequestParam(required = false) Boolean requireTapOverlap,
            @RequestParam(required = false) Boolean ratedPowerRatio,
            @RequestParam(required = false) Double ukLimit,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String ward) {
        return dashboardService.getCompatibilityResults(organisation, manufacturer, voltage, status,
                sameSiteOnly != null && sameSiteOnly,
                requireTapOverlap != null && requireTapOverlap,
                ratedPowerRatio != null && ratedPowerRatio,
                ukLimit != null ? ukLimit : 10.0, city, ward);
    }

    @GetMapping("/quality/summary")
    public DataQualitySummary getDataQualitySummary(
            @RequestParam(required = false) String organisation,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) String voltage) {
        return dashboardService.getDataQualitySummary(organisation, manufacturer, voltage);
    }

    @GetMapping("/quality/completeness-by-category")
    public List<ChartItem> getCompletenessByCategory(
            @RequestParam(required = false) String organisation,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) String voltage) {
        return dashboardService.getCompletenessByCategory(organisation, manufacturer, voltage);
    }

    @GetMapping("/quality/status-distribution")
    public List<ChartItem> getQualityStatusDistribution(
            @RequestParam(required = false) String organisation,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) String voltage) {
        return dashboardService.getQualityStatusDistribution(organisation, manufacturer, voltage);
    }

    @GetMapping("/quality/asset-count-by-phase")
    public List<PhaseCount> getAssetCountByPhase() {
        return dashboardService.getAssetCountByPhase();
    }

    @GetMapping("/quality/missing-key-fields")
    public List<MissingKeyField> getMissingKeyFields(@RequestParam(required = false) String assetType) {
        return dashboardService.getMissingKeyFields(assetType);
    }

    @GetMapping("/plants/summary")
    public PlantSummary getPlantSummary(
            @RequestParam(required = false) String organisation,
            @RequestParam(required = false) String plantType,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String plant,
            @RequestParam(required = false) String unit,
            @RequestParam(required = false) String assetClass) {
        return dashboardService.getPlantSummary(organisation, plantType, region, plant, unit, assetClass);
    }

    @GetMapping("/plants/capacity-by-type")
    public List<ChartItem> getCapacityByPlantType(
            @RequestParam(required = false) String organisation,
            @RequestParam(required = false) String plantType,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String plant) {
        return dashboardService.getCapacityByPlantType(organisation, plantType, region, plant);
    }

    @GetMapping("/plants/health-distribution")
    public List<ChartItem> getHealthDistribution(
            @RequestParam(required = false) String organisation,
            @RequestParam(required = false) String plantType,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String plant) {
        return dashboardService.getHealthDistribution(organisation, plantType, region, plant);
    }

    @GetMapping("/plants/critical-assets")
    public List<KeyValueItem> getCriticalAssetsBySystem(
            @RequestParam(required = false) String organisation,
            @RequestParam(required = false) String plantType,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String plant) {
        return dashboardService.getCriticalAssetsBySystem(organisation, plantType, region, plant);
    }

    @GetMapping("/plants/completeness")
    public List<KeyValueItem> getPlantCompleteness(
            @RequestParam(required = false) String organisation,
            @RequestParam(required = false) String plantType,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String plant) {
        return dashboardService.getPlantCompleteness(organisation, plantType, region, plant);
    }

    @GetMapping("/organisations")
    public List<Organisation> getOrganisations() {
        return organisationRepo.findAll();
    }

    @GetMapping("/organisations/with-data")
    public List<Map<String, Object>> getOrganisationsWithData() {
        List<Object[]> rows = organisationRepo.findOrganisationsWithTransformers();
        return rows.stream().map(r -> {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("id", r[0]);
            map.put("name", r[1]);
            map.put("shortName", r[2]);
            return map;
        }).collect(java.util.stream.Collectors.toList());
    }

    @GetMapping("/substations")
    public List<Substation> getSubstations() {
        return substationRepo.findAll();
    }

    @GetMapping("/manufacturers")
    public List<Manufacturer> getManufacturers() {
        return manufacturerRepo.findAll();
    }

    @GetMapping("/cities")
    public List<String> getCities() {
        java.util.TreeMap<String, String> dedup = new java.util.TreeMap<>();
        for (String raw : addressDetailRepo.findDistinctCities()) {
            String fixed = fixEncoding(raw);
            if (fixed == null) continue;
            String key = java.text.Normalizer.normalize(fixed, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase().replaceAll("[^a-z]", "");
            if (key.length() >= 3 && !dedup.containsKey(key)) {
                dedup.put(key, fixed);
            }
        }
        return new java.util.ArrayList<>(dedup.values());
    }

    @GetMapping("/wards")
    public List<String> getWards(@RequestParam(required = false) String city) {
        java.util.TreeMap<String, String> dedup = new java.util.TreeMap<>();
        for (String raw : addressDetailRepo.findDistinctWardsByCity(city)) {
            String fixed = fixEncoding(raw);
            if (fixed == null) continue;
            String key = java.text.Normalizer.normalize(fixed, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase().replaceAll("[^a-z0-9]", "");
            if (key.length() >= 3 && !dedup.containsKey(key)) {
                dedup.put(key, fixed);
            }
        }
        return new java.util.ArrayList<>(dedup.values());
    }

    private String fixEncoding(String s) {
        if (s == null || s.isEmpty()) return s;
        s = s.trim();
        boolean allLow = true;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) > 255) { allLow = false; break; }
        }
        if (allLow) {
            try {
                byte[] bytes = new byte[s.length()];
                for (int i = 0; i < s.length(); i++) bytes[i] = (byte) s.charAt(i);
                return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception ignored) {}
        }
        return s;
    }
}
