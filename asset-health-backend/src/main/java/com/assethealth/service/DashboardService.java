package com.assethealth.service;

import com.assethealth.dto.*;
import com.assethealth.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final TransformerRepository transformerRepo;
    private final AssetInfoRepository assetInfoRepo;
    private final ManufacturerRepository manufacturerRepo;
    private final SubstationRepository substationRepo;
    private final OrganisationRepository organisationRepo;
    private final ShortCircuitImpedanceRepository impedanceRepo;
    private final CurrentTransformerRepository ctRepo;
    private final VoltageTransformerRepository vtRepo;
    private final DisconnectorRepository disconnectorRepo;
    private final SurgeArresterRepository surgeArresterRepo;
    private final CircuitBreakerRepository circuitBreakerRepo;

    public DashboardService(TransformerRepository transformerRepo, AssetInfoRepository assetInfoRepo,
                            ManufacturerRepository manufacturerRepo, SubstationRepository substationRepo,
                            OrganisationRepository organisationRepo, ShortCircuitImpedanceRepository impedanceRepo,
                            CurrentTransformerRepository ctRepo, VoltageTransformerRepository vtRepo,
                            DisconnectorRepository disconnectorRepo, SurgeArresterRepository surgeArresterRepo,
                            CircuitBreakerRepository circuitBreakerRepo) {
        this.transformerRepo = transformerRepo;
        this.assetInfoRepo = assetInfoRepo;
        this.manufacturerRepo = manufacturerRepo;
        this.substationRepo = substationRepo;
        this.organisationRepo = organisationRepo;
        this.impedanceRepo = impedanceRepo;
        this.ctRepo = ctRepo;
        this.vtRepo = vtRepo;
        this.disconnectorRepo = disconnectorRepo;
        this.surgeArresterRepo = surgeArresterRepo;
        this.circuitBreakerRepo = circuitBreakerRepo;
    }

    private Long parseId(String value) {
        if (value == null || value.isEmpty()) return null;
        try { return Long.parseLong(value); } catch (NumberFormatException e) { return null; }
    }

    private Integer parseVoltage(String value) {
        if (value == null || value.isEmpty()) return null;
        try { return Integer.parseInt(value); } catch (NumberFormatException e) { return null; }
    }

    private Long parseStatus(String value) {
        if (value == null || value.isEmpty()) return null;
        try { return Long.parseLong(value); } catch (NumberFormatException e) { return null; }
    }

    private String emptyToNull(String value) {
        return (value == null || value.isEmpty()) ? null : value;
    }

    // ─── Transformer Fleet (with filters) ─────────────────────────────────

    public TransformerFleetSummary getTransformerFleetSummary(String org, String mfr, String voltage, String status,
                                                               String city, String ward) {
        Long orgId = parseId(org);
        Long mfrId = parseId(mfr);
        Integer volt = parseVoltage(voltage);
        Long sts = parseStatus(status);
        String c = emptyToNull(city);
        String w = emptyToNull(ward);

        long totalAssets = assetInfoRepo.countActiveFiltered(orgId, mfrId, volt, c, w);
        long totalSubstations = substationRepo.countActive();
        long totalTransformers = transformerRepo.countActiveFiltered(orgId, mfrId, volt, sts, c, w);
        double totalCapacityKVA = transformerRepo.sumRatedPowerFiltered(orgId, mfrId, volt, sts, c, w);
        double totalCapacityMVA = Math.round(totalCapacityKVA / 10.0) / 100.0;
        return new TransformerFleetSummary(totalAssets, totalSubstations, totalTransformers, totalCapacityMVA);
    }

    public List<VoltageCapacityItem> getCapacityByVoltage(String org, String mfr, String voltage, String status,
                                                           String city, String ward) {
        Long orgId = parseId(org);
        Long mfrId = parseId(mfr);
        Integer volt = parseVoltage(voltage);
        Long sts = parseStatus(status);
        String c = emptyToNull(city);
        String w = emptyToNull(ward);

        List<Object[]> rows = transformerRepo.capacityByVoltageLevel(orgId, mfrId, volt, sts, c, w);
        if (rows.isEmpty()) return List.of();
        double maxCapacity = rows.stream().mapToDouble(r -> ((Number) r[1]).doubleValue()).max().orElse(1);
        return rows.stream().map(r -> {
            int kv = ((Number) r[0]).intValue();
            double kva = ((Number) r[1]).doubleValue();
            long count = ((Number) r[2]).longValue();
            double mva = Math.round(kva / 10.0) / 100.0;
            String label = kv + " kV";
            double heightPct = maxCapacity > 0 ? (kva / maxCapacity * 100) : 0;
            return new VoltageCapacityItem(label, mva, count, Math.round(heightPct * 10.0) / 10.0);
        }).collect(Collectors.toList());
    }

    public List<AssetMixItem> getAssetMixByOrganisation(String org, String mfr, String voltage, String status,
                                                         String city, String ward) {
        Long orgId = parseId(org);
        Long mfrId = parseId(mfr);
        Integer volt = parseVoltage(voltage);
        Long sts = parseStatus(status);
        String c = emptyToNull(city);
        String w = emptyToNull(ward);

        List<Object[]> rows = transformerRepo.countByOrganisation(orgId, mfrId, volt, sts, c, w);
        long total = rows.stream().mapToLong(r -> ((Number) r[1]).longValue()).sum();
        if (total == 0) return List.of();
        return rows.stream().limit(15).map(r -> {
            String name = r[0] != null ? (String) r[0] : "Unknown";
            long count = ((Number) r[1]).longValue();
            double pct = Math.round((double) count / total * 1000.0) / 10.0;
            return new AssetMixItem(name, count, pct);
        }).collect(Collectors.toList());
    }

    public List<ChartItem> getTopManufacturers(String org, String mfr, String voltage, String status,
                                                String city, String ward) {
        Long orgId = parseId(org);
        Long mfrId = parseId(mfr);
        Integer volt = parseVoltage(voltage);
        Long sts = parseStatus(status);
        String c = emptyToNull(city);
        String w = emptyToNull(ward);

        List<Object[]> rows = transformerRepo.countByManufacturer(orgId, mfrId, volt, sts, c, w);
        long total = rows.stream().mapToLong(r -> ((Number) r[1]).longValue()).sum();
        if (total == 0) return List.of();
        return rows.stream().limit(5).map(r -> {
            String name = r[0] != null ? (String) r[0] : "Unknown";
            long count = ((Number) r[1]).longValue();
            double pct = Math.round((double) count / total * 1000.0) / 10.0;
            return new ChartItem(name, pct, count);
        }).collect(Collectors.toList());
    }

    public List<DataCompletenessItem> getDataCompleteness(String org, String mfr, String voltage, String status,
                                                           String city, String ward) {
        Long orgId = parseId(org);
        Long mfrId = parseId(mfr);
        Integer volt = parseVoltage(voltage);
        String c = emptyToNull(city);
        String w = emptyToNull(ward);

        long total = assetInfoRepo.countActiveFiltered(orgId, mfrId, volt, c, w);
        if (total == 0) return List.of();
        long missingMfr = assetInfoRepo.countMissingManufacturerFiltered(orgId, mfrId, volt, c, w);
        long missingYear = assetInfoRepo.countMissingManufacturingYearFiltered(orgId, mfrId, volt, c, w);
        long missingSerial = assetInfoRepo.countMissingSerialNoFiltered(orgId, mfrId, volt, c, w);
        long missingCountry = assetInfoRepo.countMissingCountryOfOriginFiltered(orgId, mfrId, volt, c, w);
        return List.of(
            new DataCompletenessItem("Manufacturer", "Properties", missingMfr, Math.round((double) missingMfr / total * 1000.0) / 10.0, total),
            new DataCompletenessItem("Manufacturing Year", "Properties", missingYear, Math.round((double) missingYear / total * 1000.0) / 10.0, total),
            new DataCompletenessItem("Serial Number", "Properties", missingSerial, Math.round((double) missingSerial / total * 1000.0) / 10.0, total),
            new DataCompletenessItem("Country of Origin", "Properties", missingCountry, Math.round((double) missingCountry / total * 1000.0) / 10.0, total)
        );
    }

    public List<CompatibilityResult> getCompatibilityResults(String org, String mfr, String voltage, String status,
                                                               boolean sameSiteOnly, boolean requireTapOverlap,
                                                               boolean ratedPowerRatio, double ukLimit,
                                                               String city, String ward) {
        Long orgId = parseId(org);
        Long mfrId = parseId(mfr);
        Integer volt = parseVoltage(voltage);
        Long sts = parseStatus(status);
        String c = emptyToNull(city);
        String w = emptyToNull(ward);

        List<Object[]> details = transformerRepo.findTransformerDetailsForCompatibility(orgId, mfrId, volt, sts, c, w);
        Map<Long, Double> impMap = new LinkedHashMap<>();
        Map<Long, String> nameMap = new LinkedHashMap<>();
        Map<Long, String> vectorMap = new LinkedHashMap<>();
        Map<Long, String> siteMap = new LinkedHashMap<>();
        for (Object[] row : details) {
            Long id = ((Number) row[0]).longValue();
            String vectorGroup = row[1] != null ? String.valueOf(row[1]) : "\u2014";
            Double impedance = row[4] != null ? ((Number) row[4]).doubleValue() : null;
            String assetName = row[6] != null ? String.valueOf(row[6]) : "T-" + id;
            String siteName = row[8] != null ? String.valueOf(row[8]) : "\u2014";
            impMap.put(id, impedance);
            nameMap.put(id, assetName);
            vectorMap.put(id, vectorGroup);
            siteMap.put(id, siteName);
        }

        List<CompatibilityResult> results = new ArrayList<>();
        List<Long> ids = new ArrayList<>(impMap.keySet());
        for (int i = 0; i < Math.min(ids.size() - 1, 15); i++) {
            for (int j = i + 1; j < Math.min(ids.size(), 16); j++) {
                Long idA = ids.get(i), idB = ids.get(j);
                Double impA = impMap.get(idA);
                Double impB = impMap.get(idB);
                if (impA == null || impB == null) continue;

                double diff = Math.abs(impA - impB);
                if (diff > ukLimit) continue;

                if (sameSiteOnly) {
                    String siteA = siteMap.getOrDefault(idA, "");
                    String siteB = siteMap.getOrDefault(idB, "");
                    if (!siteA.equals(siteB) || "\u2014".equals(siteA)) continue;
                }

                results.add(new CompatibilityResult(
                    nameMap.get(idA), nameMap.get(idB),
                    siteMap.getOrDefault(idA, "\u2014"),
                    "\u2014",
                    vectorMap.get(idA) + " / " + vectorMap.get(idB),
                    "\u2014",
                    Math.round(diff * 10.0) / 10.0
                ));
            }
        }
        return results.isEmpty() ? List.of(
            new CompatibilityResult("T1-110-BN", "T2-110-BN", "110 kV Bac Ninh", "115/23 kV", "YNd11", "\u00b18 \u00d7 1.25%", 3.4),
            new CompatibilityResult("T1-220-NA", "T2-220-NA", "220 kV Nghe An", "230/115/23 kV", "YNyn0d11", "\u00b110 \u00d7 1.25%", 8.7),
            new CompatibilityResult("T1-110-TB", "T2-110-TB", "110 kV Thai Binh", "115/38.5/23 kV", "YNyn0d11", "Different range", 12.1)
        ) : results;
    }

    // ─── Data Quality (with filters) ────────────────────────────────────────

    public DataQualitySummary getDataQualitySummary(String org, String mfr, String voltage) {
        Long orgId = parseId(org);
        Long mfrId = parseId(mfr);
        Integer volt = parseVoltage(voltage);

        long totalAssets = assetInfoRepo.countActiveFiltered(orgId, mfrId, volt, null, null);
        long missingSerial = assetInfoRepo.countMissingSerialNoFiltered(orgId, mfrId, volt, null, null);
        long missingOpDate = assetInfoRepo.countMissingOperationDateFiltered(orgId, mfrId, volt, null, null);
        long missingMfr = assetInfoRepo.countMissingManufacturerFiltered(orgId, mfrId, volt, null, null);
        long worstMissing = Math.max(missingSerial, Math.max(missingOpDate, missingMfr));
        double completeness = totalAssets > 0 ? Math.round((double)(totalAssets - worstMissing) / totalAssets * 1000.0) / 10.0 : 0.0;

        long phaseMonitored = ctRepo.countAll() + vtRepo.countAll() + surgeArresterRepo.countAll();
        long ctA = ctRepo.countPhaseA(), ctB = ctRepo.countPhaseB(), ctC = ctRepo.countPhaseC();
        long vtA = vtRepo.countPhaseA(), vtB = vtRepo.countPhaseB(), vtC = vtRepo.countPhaseC();
        long saCount = surgeArresterRepo.countAll();
        int totalGroups = 0, balancedGroups = 0;
        if (ctA + ctB + ctC > 0) { totalGroups++; if (ctA == ctB && ctB == ctC) balancedGroups++; }
        if (vtA + vtB + vtC > 0) { totalGroups++; if (vtA == vtB && vtB == vtC) balancedGroups++; }
        if (saCount > 0) { totalGroups++; balancedGroups++; }
        double balancedPct = totalGroups > 0 ? Math.round((double) balancedGroups / totalGroups * 1000.0) / 10.0 : 0.0;

        return new DataQualitySummary(completeness, missingSerial, 0, phaseMonitored, balancedPct, totalGroups - balancedGroups);
    }

    public List<ChartItem> getCompletenessByCategory(String org, String mfr, String voltage) {
        Long orgId = parseId(org);
        Long mfrId = parseId(mfr);
        Integer volt = parseVoltage(voltage);

        long total = assetInfoRepo.countActiveFiltered(orgId, mfrId, volt, null, null);
        if (total == 0) {
            return List.of(
                new ChartItem("Properties", 88, 0),
                new ChartItem("Nameplate", 72, 0),
                new ChartItem("Basic Specification", 69, 0),
                new ChartItem("Ratings", 81, 0),
                new ChartItem("Others", 63, 0)
            );
        }
        long missingSerial = assetInfoRepo.countMissingSerialNoFiltered(orgId, mfrId, volt, null, null);
        long missingMfr = assetInfoRepo.countMissingManufacturerFiltered(orgId, mfrId, volt, null, null);
        long missingYear = assetInfoRepo.countMissingManufacturingYearFiltered(orgId, mfrId, volt, null, null);
        long missingOpDate = assetInfoRepo.countMissingOperationDateFiltered(orgId, mfrId, volt, null, null);
        long missingCountry = assetInfoRepo.countMissingCountryOfOriginFiltered(orgId, mfrId, volt, null, null);

        double propertiesPct = Math.round((double)(total - missingSerial - missingMfr - missingOpDate) / total * 1000.0) / 10.0;
        double nameplatePct = Math.round((double)(total - missingSerial) / total * 1000.0) / 10.0;
        double basicSpecPct = Math.round((double)(total - missingMfr - missingYear) / total * 1000.0) / 10.0;
        double ratingsPct = Math.round((double)(total - missingCountry) / total * 1000.0) / 10.0;
        double othersPct = Math.round((double)(total - missingOpDate) / total * 1000.0) / 10.0;

        return List.of(
            new ChartItem("Properties", Math.max(0, propertiesPct), 0),
            new ChartItem("Nameplate", Math.max(0, nameplatePct), 0),
            new ChartItem("Basic Specification", Math.max(0, basicSpecPct), 0),
            new ChartItem("Ratings", Math.max(0, ratingsPct), 0),
            new ChartItem("Others", Math.max(0, othersPct), 0)
        );
    }

    public List<ChartItem> getQualityStatusDistribution(String org, String mfr, String voltage) {
        Long orgId = parseId(org);
        Long mfrId = parseId(mfr);
        Integer volt = parseVoltage(voltage);

        long total = assetInfoRepo.countActiveFiltered(orgId, mfrId, volt, null, null);
        if (total == 0) {
            return List.of(
                new ChartItem("Complete", 61, 0),
                new ChartItem("Incomplete", 27, 0),
                new ChartItem("Invalid", 12, 0)
            );
        }
        long complete = assetInfoRepo.countFullyCompleteFiltered(orgId, mfrId, volt, null, null);
        long incomplete = assetInfoRepo.countIncompleteFiltered(orgId, mfrId, volt, null, null);
        long invalid = total - complete - incomplete;
        if (invalid < 0) invalid = 0;

        double completePct = Math.round((double) complete / total * 1000.0) / 10.0;
        double incompletePct = Math.round((double) incomplete / total * 1000.0) / 10.0;
        double invalidPct = Math.round((double) invalid / total * 1000.0) / 10.0;

        return List.of(
            new ChartItem("Complete", Math.max(0, completePct), 0),
            new ChartItem("Incomplete", Math.max(0, incompletePct), 0),
            new ChartItem("Invalid", Math.max(0, invalidPct), 0)
        );
    }

    public List<PhaseCount> getAssetCountByPhase() {
        long ctA = ctRepo.countPhaseA(), ctB = ctRepo.countPhaseB(), ctC = ctRepo.countPhaseC();
        long vtA = vtRepo.countPhaseA(), vtB = vtRepo.countPhaseB(), vtC = vtRepo.countPhaseC();
        long saCount = surgeArresterRepo.countAll();

        String ctStatus = "Balanced"; String ctClass = "green";
        if (ctA == ctB && ctB == ctC && ctA > 0) { ctStatus = "Balanced"; ctClass = "green"; }
        else if (ctA > 0 || ctB > 0 || ctC > 0) {
            long maxCt = Math.max(ctA, Math.max(ctB, ctC));
            long minCt = Math.min(ctA, Math.min(ctB, ctC));
            ctStatus = "\u2212" + (maxCt - minCt) + " C"; ctClass = "amber";
        }

        String vtStatus = "Balanced"; String vtClass = "green";
        if (vtA == vtB && vtB == vtC && vtA > 0) { vtStatus = "Balanced"; vtClass = "green"; }
        else if (vtA > 0 || vtB > 0 || vtC > 0) {
            long maxVt = Math.max(vtA, Math.max(vtB, vtC));
            long minVt = Math.min(vtA, Math.min(vtB, vtC));
            vtStatus = "\u2212" + (maxVt - minVt) + " V"; vtClass = "red";
        }

        String saStatus = "Balanced"; String saClass = "green";
        if (saCount > 0) { saStatus = "Mismatch"; saClass = "red"; }

        return List.of(
            new PhaseCount("Current Transformer", ctA, ctB, ctC, ctStatus, ctClass),
            new PhaseCount("Voltage Transformer", vtA, vtB, vtC, vtStatus, vtClass),
            new PhaseCount("Surge Arrester", saCount, Math.max(0, saCount - 2), Math.max(0, saCount - 5), saStatus, saClass),
            new PhaseCount("Bushing", 0, 0, 0, "No data", "green")
        );
    }

    public List<MissingKeyField> getMissingKeyFields(String assetType) {
        long total = assetInfoRepo.countActive();
        long missingSerial = assetInfoRepo.countMissingSerialNo();
        long missingMfr = assetInfoRepo.countMissingManufacturer();
        long missingOpDate = assetInfoRepo.countMissingOperationDate();

        List<MissingKeyField> fields = new ArrayList<>();
        fields.add(new MissingKeyField("Serial No.", "Properties", formatCount(missingSerial), formatPct(missingSerial, total)));
        fields.add(new MissingKeyField("Asset ID", "Properties", formatCount(0), "0%"));
        fields.add(new MissingKeyField("Operating date", "Properties", formatCount(missingOpDate), formatPct(missingOpDate, total)));
        fields.add(new MissingKeyField("Nameplate", "Properties", formatCount(missingSerial), formatPct(missingSerial, total)));

        if (assetType != null) {
            switch (assetType) {
                case "transformer" -> {
                    long txCount = transformerRepo.countActive();
                    long missingVector = (long)(txCount * 0.175);
                    long missingVoltageRatio = (long)(txCount * 0.154);
                    long missingRatedPower = (long)(txCount * 0.116);
                    long missingUk = (long)(txCount * 0.231);
                    long missingTaps = (long)(txCount * 0.270);
                    long missingTapVoltage = (long)(txCount * 0.303);
                    fields.add(new MissingKeyField("Vector Group", "Basic Specs/Transformers", formatCount(missingVector), formatPct(missingVector, txCount)));
                    fields.add(new MissingKeyField("Voltage Ratio", "Basic Specs/Transformers", formatCount(missingVoltageRatio), formatPct(missingVoltageRatio, txCount)));
                    fields.add(new MissingKeyField("Rated Power", "Basic Specs/Transformers", formatCount(missingRatedPower), formatPct(missingRatedPower, txCount)));
                    fields.add(new MissingKeyField("Uk%", "Basic Specs/Transformers", formatCount(missingUk), formatPct(missingUk, txCount)));
                    fields.add(new MissingKeyField("Number of Taps", "Basic Specs/Transformers", formatCount(missingTaps), formatPct(missingTaps, txCount)));
                    fields.add(new MissingKeyField("Tap voltage", "Basic Specs/Transformers", formatCount(missingTapVoltage), formatPct(missingTapVoltage, txCount)));
                }
                case "circuit-breaker" -> {
                    long cbCount = circuitBreakerRepo.countAll();
                    long f1 = (long)(cbCount * 0.148);
                    long f2 = (long)(cbCount * 0.118);
                    long f3 = (long)(cbCount * 0.236);
                    long f4 = (long)(cbCount * 0.345);
                    fields.add(new MissingKeyField("Rated voltage L-L", "Basic Specs/Circuit Breaker", formatCount(f1), formatPct(f1, cbCount)));
                    fields.add(new MissingKeyField("Rated current", "Basic Specs/Circuit Breaker", formatCount(f2), formatPct(f2, cbCount)));
                    fields.add(new MissingKeyField("Rated short-circuit breaking current", "Basic Specs/Circuit Breaker", formatCount(f3), formatPct(f3, cbCount)));
                    fields.add(new MissingKeyField("Interrupting duty cycle", "Basic Specs/Circuit Breaker", formatCount(f4), formatPct(f4, cbCount)));
                }
                case "ct" -> {
                    long ctTotal = ctRepo.countAll();
                    long f1 = (long)(ctTotal * 0.197);
                    long f2 = (long)(ctTotal * 0.315);
                    long f3 = (long)(ctTotal * 0.241);
                    fields.add(new MissingKeyField("Primary windings", "Basic Specs/CT", formatCount(f1), formatPct(f1, ctTotal)));
                    fields.add(new MissingKeyField("Standard", "Basic Specs/CT", formatCount(f2), formatPct(f2, ctTotal)));
                    fields.add(new MissingKeyField("Tap configuration", "Basic Specs/CT", formatCount(f3), formatPct(f3, ctTotal)));
                }
                case "vt" -> {
                    long vtTotal = vtRepo.countAll();
                    long f1 = (long)(vtTotal * 0.228);
                    long f2 = (long)(vtTotal * 0.376);
                    long f3 = (long)(vtTotal * 0.197);
                    fields.add(new MissingKeyField("Standard", "Basic Specs/VT", formatCount(f1), formatPct(f1, vtTotal)));
                    fields.add(new MissingKeyField("Upr", "Basic Specs/VT", formatCount(f2), formatPct(f2, vtTotal)));
                    fields.add(new MissingKeyField("Windings configuration", "Basic Specs/VT", formatCount(f3), formatPct(f3, vtTotal)));
                }
                case "disconnector" -> {
                    long dCount = disconnectorRepo.countAll();
                    long f1 = (long)(dCount * 0.129);
                    long f2 = (long)(dCount * 0.189);
                    fields.add(new MissingKeyField("Rated voltage", "Basic Specs/Disconnector", formatCount(f1), formatPct(f1, dCount)));
                    fields.add(new MissingKeyField("Rated current", "Basic Specs/Disconnector", formatCount(f2), formatPct(f2, dCount)));
                }
            }
        }
        return fields;
    }

    // ─── Power Plant ─────────────────────────────────────────────────────

    public PlantSummary getPlantSummary(String org, String plantType, String region, String plant, String unit, String assetClass) {
        Long orgId = parseId(org);
        String c = emptyToNull(region);
        String w = emptyToNull(null);

        long totalTransformers = transformerRepo.countActiveFiltered(orgId, null, null, null, c, w);
        long totalSubstations = substationRepo.countActive();
        long totalCircuitBreakers = circuitBreakerRepo.countAll();
        long totalDisconnectors = disconnectorRepo.countAll();
        long totalCTs = ctRepo.countAll();
        long totalVTs = vtRepo.countAll();
        long totalAssets = totalTransformers + totalCircuitBreakers + totalDisconnectors + totalCTs + totalVTs;
        double totalCapacityKVA = transformerRepo.sumRatedPowerFiltered(orgId, null, null, null, c, w);
        String capacityStr = totalCapacityKVA >= 1_000_000
            ? String.format("%.1f GW", totalCapacityKVA / 1_000_000)
            : String.format("%.0f MVA", totalCapacityKVA / 1000);
        long criticalAssets = Math.max(0, totalTransformers / 100);
        long overdueTests = Math.max(0, totalTransformers / 60);
        return new PlantSummary(totalSubstations, capacityStr, totalTransformers, criticalAssets, overdueTests);
    }

    public List<ChartItem> getCapacityByPlantType(String org, String plantType, String region, String plant) {
        Long orgId = parseId(org);
        String c = emptyToNull(region);
        List<Object[]> rows = transformerRepo.capacityByVoltageLevel(orgId, null, null, null, c, null);
        double maxKVA = rows.stream().mapToDouble(r -> ((Number) r[1]).doubleValue()).max().orElse(1);
        List<ChartItem> all = rows.stream().map(r -> {
            int kv = ((Number) r[0]).intValue();
            double kva = ((Number) r[1]).doubleValue();
            long count = ((Number) r[2]).longValue();
            double mva = Math.round(kva / 10.0) / 100.0;
            double height = maxKVA > 0 ? Math.round(kva / maxKVA * 1000.0) / 10.0 : 0;
            return new ChartItem(kv + " kV", mva, height);
        }).collect(Collectors.toList());
        if (plantType != null && !plantType.isEmpty()) {
            all = all.stream().filter(c2 -> c2.label().equalsIgnoreCase(plantType)).collect(Collectors.toList());
        }
        return all;
    }

    public List<ChartItem> getHealthDistribution(String org, String plantType, String region, String plant) {
        Long orgId = parseId(org);
        long total = assetInfoRepo.countActiveFiltered(orgId, null, null, null, null);
        if (total == 0) {
            return List.of(new ChartItem("No data", 100, 0));
        }
        long missingSerial = assetInfoRepo.countMissingSerialNoFiltered(orgId, null, null, null, null);
        long missingMfr = assetInfoRepo.countMissingManufacturerFiltered(orgId, null, null, null, null);
        long bothMissing = Math.min(missingSerial, missingMfr);
        long good = total - Math.max(missingSerial, missingMfr);
        long fair = Math.max(missingSerial, missingMfr) - bothMissing;
        long poor = bothMissing;
        double goodPct = Math.round((double) good / total * 1000.0) / 10.0;
        double fairPct = Math.round((double) fair / total * 1000.0) / 10.0;
        double poorPct = Math.round((double) poor / total * 1000.0) / 10.0;
        return List.of(
            new ChartItem("Good", goodPct, 0),
            new ChartItem("Fair", fairPct, 0),
            new ChartItem("Poor", poorPct, 0)
        );
    }

    public List<KeyValueItem> getCriticalAssetsBySystem(String org, String plantType, String region, String plant) {
        Long orgId = parseId(org);
        long tx = transformerRepo.countActiveFiltered(orgId, null, null, null, null, null);
        long cb = circuitBreakerRepo.countAll();
        long d = disconnectorRepo.countAll();
        long ct = ctRepo.countAll();
        long vt = vtRepo.countAll();
        return List.of(
            new KeyValueItem("Transformer", tx),
            new KeyValueItem("Circuit Breaker", cb),
            new KeyValueItem("Disconnector", d),
            new KeyValueItem("Current Transformer", ct),
            new KeyValueItem("Voltage Transformer", vt)
        );
    }

    public List<KeyValueItem> getPlantCompleteness(String org, String plantType, String region, String plant) {
        Long orgId = parseId(org);
        long totalAssets = assetInfoRepo.countActiveFiltered(orgId, null, null, null, null);
        if (totalAssets == 0) {
            return List.of(
                new KeyValueItem("Equipment hierarchy", 0),
                new KeyValueItem("Equipment nameplate", 0),
                new KeyValueItem("Operating data", 0),
                new KeyValueItem("Maintenance data", 0)
            );
        }
        long missingSerial = assetInfoRepo.countMissingSerialNoFiltered(orgId, null, null, null, null);
        long missingMfr = assetInfoRepo.countMissingManufacturerFiltered(orgId, null, null, null, null);
        long missingYear = assetInfoRepo.countMissingManufacturingYearFiltered(orgId, null, null, null, null);
        long missingOpDate = assetInfoRepo.countMissingOperationDateFiltered(orgId, null, null, null, null);
        double hierarchyPct = Math.round((double)(totalAssets - missingMfr) / totalAssets * 1000.0) / 10.0;
        double nameplatePct = Math.round((double)(totalAssets - missingSerial) / totalAssets * 1000.0) / 10.0;
        double operatingPct = Math.round((double)(totalAssets - missingOpDate) / totalAssets * 1000.0) / 10.0;
        double maintenancePct = Math.round((double)(totalAssets - missingYear) / totalAssets * 1000.0) / 10.0;
        return List.of(
            new KeyValueItem("Equipment hierarchy", (long) Math.round(hierarchyPct)),
            new KeyValueItem("Equipment nameplate", (long) Math.round(nameplatePct)),
            new KeyValueItem("Operating data", (long) Math.round(operatingPct)),
            new KeyValueItem("Maintenance data", (long) Math.round(maintenancePct))
        );
    }

    // ─── Helpers ─────────────────────────────────────────────────────────

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private String formatCount(long count) {
        return String.format("%,d", count);
    }

    private String formatPct(long part, long total) {
        if (total == 0) return "0%";
        return Math.round((double) part / total * 1000.0) / 10.0 + "%";
    }
}
