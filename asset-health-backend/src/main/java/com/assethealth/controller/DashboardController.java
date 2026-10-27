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
            @RequestParam(required = false) String ward,
            @RequestParam(required = false) String substation) {
        return dashboardService.getTransformerFleetSummary(organisation, manufacturer, voltage, status, city, ward, substation);
    }

    @GetMapping("/transformers/capacity-by-voltage")
    public List<VoltageCapacityItem> getCapacityByVoltage(
            @RequestParam(required = false) String organisation,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) String voltage,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String ward,
            @RequestParam(required = false) String substation) {
        return dashboardService.getCapacityByVoltage(organisation, manufacturer, voltage, status, city, ward, substation);
    }

    @GetMapping("/transformers/mix-by-organisation")
    public List<AssetMixItem> getMixByOrganisation(
            @RequestParam(required = false) String organisation,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) String voltage,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String ward,
            @RequestParam(required = false) String substation) {
        return dashboardService.getAssetMixByOrganisation(organisation, manufacturer, voltage, status, city, ward, substation);
    }

    @GetMapping("/transformers/manufacturer-mix")
    public List<AssetMixItem> getManufacturerMix(
            @RequestParam(required = false) String organisation,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) String voltage,
            @RequestParam(required = false) String status) {
        return dashboardService.getAssetMixByOrganisation(organisation, manufacturer, voltage, status, null, null, null);
    }

    @GetMapping("/transformers/top-manufacturers")
    public List<ChartItem> getTopManufacturers(
            @RequestParam(required = false) String organisation,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) String voltage,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String ward,
            @RequestParam(required = false) String substation) {
        return dashboardService.getTopManufacturers(organisation, manufacturer, voltage, status, city, ward, substation);
    }

    @GetMapping("/transformers/data-completeness")
    public List<DataCompletenessItem> getDataCompleteness(
            @RequestParam(required = false) String organisation,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) String voltage,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String ward) {
        return dashboardService.getDataCompleteness(organisation, manufacturer, voltage, status, city, ward, null);
    }

    @GetMapping("/overview/summary")
    public TransformerFleetSummary getOverviewSummary(
            @RequestParam(required = false) String organisation,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String ward,
            @RequestParam(required = false) String voltage,
            @RequestParam(required = false) String assetClass,
            @RequestParam(required = false) String status) {
        return dashboardService.getOverviewSummary(organisation, city, ward, voltage, assetClass, status);
    }

    @GetMapping("/overview/capacity-by-voltage")
    public List<VoltageCapacityItem> getOverviewCapacityByVoltage(
            @RequestParam(required = false) String organisation,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String ward,
            @RequestParam(required = false) String voltage,
            @RequestParam(required = false) String assetClass,
            @RequestParam(required = false) String status) {
        return dashboardService.getOverviewCapacityByVoltage(organisation, city, ward, voltage, assetClass, status);
    }

    @GetMapping("/overview/mix-by-organisation")
    public List<AssetMixItem> getOverviewMixByOrganisation(
            @RequestParam(required = false) String organisation,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String ward,
            @RequestParam(required = false) String voltage,
            @RequestParam(required = false) String assetClass,
            @RequestParam(required = false) String status) {
        return dashboardService.getOverviewMixByOrganisation(organisation, city, ward, voltage, assetClass, status);
    }

    @GetMapping("/overview/top-manufacturers")
    public List<ChartItem> getOverviewTopManufacturers(
            @RequestParam(required = false) String organisation,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String ward,
            @RequestParam(required = false) String voltage,
            @RequestParam(required = false) String assetClass,
            @RequestParam(required = false) String status) {
        return dashboardService.getOverviewTopManufacturers(organisation, city, ward, voltage, assetClass, status);
    }

    @GetMapping("/overview/data-completeness")
    public List<DataCompletenessItem> getOverviewDataCompleteness(
            @RequestParam(required = false) String organisation,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String ward,
            @RequestParam(required = false) String voltage,
            @RequestParam(required = false) String assetClass,
            @RequestParam(required = false) String status) {
        return dashboardService.getDataCompleteness(organisation, null, voltage, status, city, ward, assetClass);
    }

    @GetMapping("/transformers/compatibility")
    public List<CompatibilityResult> getCompatibilityResults(
            @RequestParam(required = false) String organisation,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) String voltage,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean sameSiteOnly,
            @RequestParam(required = false) Boolean requireRatio,
            @RequestParam(required = false) Boolean requireVectorGroup,
            @RequestParam(required = false) Boolean requireTapConfiguration,
            @RequestParam(required = false) Boolean requireImpedance,
            @RequestParam(required = false) Boolean requireTapOverlap,
            @RequestParam(required = false) Boolean ratedPowerRatio,
            @RequestParam(required = false) Double ukLimit,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String ward,
            @RequestParam(required = false) String substation) {
        boolean tapRequired = (requireTapConfiguration != null && requireTapConfiguration)
                || (requireTapOverlap != null && requireTapOverlap);
        return dashboardService.getCompatibilityResults(organisation, manufacturer, voltage, status,
                sameSiteOnly == null || sameSiteOnly,
                requireRatio == null || requireRatio,
                requireVectorGroup == null || requireVectorGroup,
                tapRequired,
                requireImpedance == null || requireImpedance,
                ratedPowerRatio != null && ratedPowerRatio,
                ukLimit != null ? ukLimit : 10.0, city, ward, substation);
    }

    @GetMapping("/quality/summary")
    public DataQualitySummary getDataQualitySummary(
            @RequestParam(required = false) String organisation,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) String voltage,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String assetClass,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String qualityStatus) {
        return dashboardService.getDataQualitySummary(organisation, manufacturer, voltage,
            region, assetClass, category, qualityStatus);
    }

    @GetMapping("/quality/completeness-by-category")
    public List<ChartItem> getCompletenessByCategory(
            @RequestParam(required = false) String organisation,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) String voltage,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String assetClass,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String qualityStatus) {
        return dashboardService.getCompletenessByCategory(organisation, manufacturer, voltage,
            region, assetClass, category, qualityStatus);
    }

    @GetMapping("/quality/status-distribution")
    public List<ChartItem> getQualityStatusDistribution(
            @RequestParam(required = false) String organisation,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) String voltage,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String assetClass) {
        return dashboardService.getQualityStatusDistribution(organisation, manufacturer, voltage,
            region, assetClass);
    }

    @GetMapping("/quality/asset-count-by-phase")
    public List<PhaseCount> getAssetCountByPhase(
            @RequestParam(required = false) String organisation,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) String voltage,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String assetClass,
            @RequestParam(required = false) String qualityStatus) {
        return dashboardService.getAssetCountByPhase(organisation, manufacturer, voltage,
            region, assetClass, qualityStatus);
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

    @GetMapping("/substations/with-transformers")
    public List<Map<String, Object>> getSubstationsWithTransformers(
            @RequestParam(required = false) String organisation,
            @RequestParam(required = false) String city) {
        Long orgId = null;
        if (organisation != null && !organisation.isEmpty()) {
            try { orgId = Long.parseLong(organisation); } catch (NumberFormatException ignored) {}
        }
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Object[] row : substationRepo.findSubstationsWithTransformers(orgId)) {
            String rawCity = row[2] != null ? String.valueOf(row[2]) : "";
            String fixedCity = fixEncoding(rawCity);
            if (city != null && !city.isEmpty() && !city.equals(fixedCity)) continue;
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("id", row[0]);
            map.put("name", row[1]);
            map.put("city", fixedCity);
            result.add(map);
        }
        return result;
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
            if (fixed == null || fixed.isEmpty()) continue;
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
        java.util.List<String> rawList;
        if (city != null && !city.isEmpty()) {
            rawList = addressDetailRepo.findDistinctWardsByCity(city);
        } else {
            rawList = addressDetailRepo.findDistinctWards();
        }
        for (String raw : rawList) {
            String fixed = fixEncoding(raw);
            if (fixed == null || fixed.isEmpty()) continue;
            String key = java.text.Normalizer.normalize(fixed, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase().replaceAll("[^a-z0-9]", "");
            if (key.length() >= 3 && !dedup.containsKey(key)) {
                dedup.put(key, fixed);
            }
        }
        return new java.util.ArrayList<>(dedup.values());
    }

    @GetMapping("/debug/encoding")
    public java.util.Map<String, Object> debugEncoding() {
        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        List<String> cities = addressDetailRepo.findDistinctCities();
        java.util.List<java.util.Map<String, Object>> samples = new java.util.ArrayList<>();
        int count = 0;
        for (String raw : cities) {
            if (count++ >= 5) break;
            java.util.Map<String, Object> sample = new java.util.LinkedHashMap<>();
            sample.put("raw", raw);
            sample.put("fixed", fixEncoding(raw));
            sample.put("length", raw.length());
            int[] codepoints = new int[Math.min(raw.length(), 20)];
            for (int i = 0; i < codepoints.length; i++) codepoints[i] = raw.charAt(i);
            sample.put("codepoints", java.util.Arrays.stream(codepoints).boxed().collect(java.util.stream.Collectors.toList()));
            samples.add(sample);
        }
        result.put("samples", samples);
        result.put("dbEncoding", addressDetailRepo.findDbEncoding());
        return result;
    }

    private String fixEncoding(String s) {
        if (s == null || s.isEmpty()) return s;
        s = s.trim();
        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            for (int i = 0; i < s.length(); i++) {
                int cp = s.codePointAt(i);
                if (cp < 128) {
                    baos.write(cp);
                } else if (cp <= 0x7FF) {
                    baos.write(0xC0 | (cp >> 6));
                    baos.write(0x80 | (cp & 0x3F));
                } else if (cp <= 0xFFFF) {
                    baos.write(0xE0 | (cp >> 12));
                    baos.write(0x80 | ((cp >> 6) & 0x3F));
                    baos.write(0x80 | (cp & 0x3F));
                } else {
                    baos.write(0xF0 | (cp >> 18));
                    baos.write(0x80 | ((cp >> 12) & 0x3F));
                    baos.write(0x80 | ((cp >> 6) & 0x3F));
                    baos.write(0x80 | (cp & 0x3F));
                }
                if (Character.isSupplementaryCodePoint(cp)) i++;
            }
            byte[] utf8 = baos.toByteArray();
            return new String(utf8, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }
}
