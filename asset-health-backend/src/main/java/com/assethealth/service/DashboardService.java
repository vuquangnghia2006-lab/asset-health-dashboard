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

    private record ParallelCandidate(long id, String name, String vectorGroup, Long siteId, String siteName) {}
    private record TapPoint(Integer position, Double kv) {}
    private record TapProfile(String winding, String type, Integer tapCount, String scheme, List<TapPoint> schedule) {}
    private record ImpedancePoint(String type, Double value) {}
    private record ParallelScreeningInputs(
        Map<Long, ParallelCandidate> transformers,
        Map<Long, Map<String, Double>> ratios,
        Map<Long, List<TapProfile>> taps,
        Map<Long, Map<String, List<Double>>> impedances,
        Map<Long, List<Double>> ratedPowers,
        boolean sameSiteOnly,
        boolean requireRatio,
        boolean requireVectorGroup,
        boolean requireTapConfiguration,
        boolean requireImpedance,
        boolean ratedPowerRatio,
        double ukLimit
    ) {}

    private static String cleanText(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static String normalizeWinding(Object raw) {
        String value = cleanText(raw).toUpperCase(java.util.Locale.ROOT);
        return switch (value) {
            case "PRIM", "PRIMARY", "PRI", "P", "HV" -> "PRIM";
            case "SEC", "SECONDARY", "S", "LV" -> "SEC";
            case "TERT", "TERTIARY", "T", "TV" -> "TERT";
            default -> value;
        };
    }

    private static String normalizeTapType(Object raw) {
        String value = cleanText(raw).toUpperCase(java.util.Locale.ROOT);
        return switch (value) {
            case "OLTC", "ON LOAD", "ON-LOAD", "ONLOAD", "ON LOAD TAP CHANGER", "ON-LOAD TAP CHANGER" -> "OLTC";
            case "DETC", "OFF CIRCUIT", "OFF-CIRCUIT", "OFFCIRCUIT", "DE-ENERGIZED", "DEENERGIZED",
                "DE ENERGIZED", "OFF LOAD", "OFF-LOAD", "NO LOAD", "NO-LOAD", "NLTC" -> "DETC";
            default -> value;
        };
    }

    private static Double toKilovolts(Object value, Object unit) {
        if (!(value instanceof Number number)) return null;
        double volts = number.doubleValue();
        String unitText = cleanText(unit).toUpperCase(java.util.Locale.ROOT);
        return switch (unitText) {
            case "", "KV", "KILOVOLT", "KILOVOLTS" -> volts;
            case "V", "VOLT", "VOLTS" -> volts / 1000.0;
            case "MV", "MEGAVOLT", "MEGAVOLTS" -> volts * 1000.0;
            default -> null;
        };
    }

    private static Double toKva(Object value, Object unit) {
        if (!(value instanceof Number number)) return null;
        double power = number.doubleValue();
        String unitText = cleanText(unit).toUpperCase(java.util.Locale.ROOT);
        return switch (unitText) {
            case "", "KVA" -> power;
            case "VA" -> power / 1000.0;
            case "MVA" -> power * 1000.0;
            default -> null;
        };
    }

    private static boolean isPercentUnit(Object unit) {
        String unitText = cleanText(unit).toUpperCase(java.util.Locale.ROOT);
        return unitText.isEmpty() || unitText.equals("%") || unitText.equals("PERCENT") || unitText.equals("PCT");
    }

    private static Double relativePercent(Double first, Double second) {
        if (first == null || second == null) return null;
        double scale = Math.max(Math.abs(first), Math.abs(second));
        if (scale == 0) return 0.0;
        return Math.abs(first - second) / scale * 100.0;
    }

    private static double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private static String formatKv(double kv) {
        if (kv == Math.rint(kv)) return Long.toString(Math.round(kv)) + " kV";
        return String.format(java.util.Locale.US, "%.1f kV", kv);
    }

    private static String ratioSignature(Map<String, Double> ratio) {
        return ratio.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> entry.getKey() + "=" + String.format(java.util.Locale.US, "%.3f", entry.getValue()))
            .collect(Collectors.joining("|"));
    }

    private static String ratioText(Map<String, Double> ratio) {
        List<String> parts = new ArrayList<>();
        for (String winding : List.of("PRIM", "SEC", "TERT")) {
            if (ratio.containsKey(winding)) parts.add(formatKv(ratio.get(winding)).replace(" kV", ""));
        }
        return parts.isEmpty() ? "—" : String.join("/", parts) + " kV";
    }

    private static String tapProfileKey(TapProfile profile) {
        String schedule = profile.schedule().stream()
            .map(point -> (point.position() == null ? "?" : point.position().toString())
                + "=" + String.format(java.util.Locale.US, "%.3f", point.kv()))
            .collect(Collectors.joining(","));
        return profile.winding() + "|" + profile.type() + "|"
            + (profile.tapCount() == null ? "?" : profile.tapCount().toString()) + "|"
            + profile.scheme() + "|" + schedule;
    }

    private static String tapScheduleText(List<TapProfile> profiles, Map<String, Double> ratio) {
        if (profiles.isEmpty()) return "—";
        List<String> texts = new ArrayList<>();
        for (TapProfile profile : profiles) {
            List<Double> voltages = profile.schedule().stream()
                .map(TapPoint::kv)
                .sorted()
                .collect(Collectors.toCollection(ArrayList::new));
            if (voltages.isEmpty()) {
                texts.add(profile.tapCount() == null ? "Tap data incomplete" : profile.tapCount() + " taps");
                continue;
            }
            Double nominal = ratio.get(profile.winding());
            if (nominal == null || nominal <= 0) nominal = voltages.get(voltages.size() / 2);
            List<Double> steps = new ArrayList<>();
            for (int i = 1; i < voltages.size(); i++) {
                steps.add(Math.abs(voltages.get(i) - voltages.get(i - 1)) / nominal * 100.0);
            }
            boolean uniform = !steps.isEmpty()
                && steps.stream().allMatch(step -> Math.abs(step - steps.get(0)) < 0.05);
            if (uniform && voltages.size() % 2 == 1
                && Math.abs(nominal - voltages.get(voltages.size() / 2)) < nominal * 0.02) {
                texts.add("±" + ((voltages.size() - 1) / 2) + " × "
                    + String.format(java.util.Locale.US, "%.2f%%", roundOneDecimal(steps.get(0))));
            } else {
                texts.add(formatKv(voltages.get(0)) + "–" + formatKv(voltages.get(voltages.size() - 1)));
            }
        }
        return String.join(" | ", texts);
    }

    private static CompatibilityResult scoreParallelPair(long idA, long idB, ParallelScreeningInputs inputs) {
        ParallelCandidate candidateA = inputs.transformers().get(idA);
        ParallelCandidate candidateB = inputs.transformers().get(idB);
        Map<String, Double> ratioA = inputs.ratios().getOrDefault(idA, Map.of());
        Map<String, Double> ratioB = inputs.ratios().getOrDefault(idB, Map.of());
        List<TapProfile> tapsA = inputs.taps().getOrDefault(idA, List.of());
        List<TapProfile> tapsB = inputs.taps().getOrDefault(idB, List.of());
        Map<String, List<Double>> impedanceA = inputs.impedances().getOrDefault(idA, Map.of());
        Map<String, List<Double>> impedanceB = inputs.impedances().getOrDefault(idB, Map.of());
        List<Double> powerA = inputs.ratedPowers().getOrDefault(idA, List.of());
        List<Double> powerB = inputs.ratedPowers().getOrDefault(idB, List.of());

        boolean siteKnown = candidateA.siteId() != null && candidateB.siteId() != null;
        boolean siteOk = !inputs.sameSiteOnly() || (siteKnown && candidateA.siteId().equals(candidateB.siteId()));

        boolean ratioKnown = !ratioA.isEmpty() && !ratioB.isEmpty();
        boolean ratioOk = ratioKnown && ratioSignature(ratioA).equals(ratioSignature(ratioB));

        String vectorA = cleanText(candidateA.vectorGroup());
        String vectorB = cleanText(candidateB.vectorGroup());
        boolean vectorKnown = !vectorA.isEmpty() && !vectorB.isEmpty();
        boolean vectorOk = vectorKnown && vectorA.equals(vectorB);

        boolean tapKnown = !tapsA.isEmpty() && !tapsB.isEmpty()
            && tapsA.stream().allMatch(profile -> profile.tapCount() != null && !profile.schedule().isEmpty())
            && tapsB.stream().allMatch(profile -> profile.tapCount() != null && !profile.schedule().isEmpty());
        List<String> tapKeysA = tapsA.stream().map(DashboardService::tapProfileKey).sorted().toList();
        List<String> tapKeysB = tapsB.stream().map(DashboardService::tapProfileKey).sorted().toList();
        boolean tapOk = tapKnown && tapKeysA.equals(tapKeysB);

        Double maxUkDiff = null;
        boolean impedanceKnown = false;
        for (String type : impedanceA.keySet()) {
            if (!impedanceB.containsKey(type)) continue;
            for (Double valueA : impedanceA.get(type)) {
                for (Double valueB : impedanceB.get(type)) {
                    Double difference = relativePercent(valueA, valueB);
                    if (difference == null) continue;
                    impedanceKnown = true;
                    if (maxUkDiff == null || difference > maxUkDiff) maxUkDiff = difference;
                }
            }
        }

        boolean powerKnown = !powerA.isEmpty() && !powerB.isEmpty();
        boolean powerOk = true;
        if (inputs.ratedPowerRatio() && powerKnown) {
            double smallest = Math.min(Collections.min(powerA), Collections.min(powerB));
            double largest = Math.max(Collections.max(powerA), Collections.max(powerB));
            powerOk = smallest > 0 && largest / smallest <= 3.0;
        }

        boolean hardFailure =
            (inputs.sameSiteOnly() && siteKnown && !siteOk)
            || (inputs.requireRatio() && ratioKnown && !ratioOk)
            || (inputs.requireVectorGroup() && vectorKnown && !vectorOk)
            || (inputs.requireTapConfiguration() && tapKnown && !tapOk)
            || (inputs.ratedPowerRatio() && powerKnown && !powerOk)
            || (inputs.requireImpedance() && maxUkDiff != null && maxUkDiff > inputs.ukLimit());
        boolean unknownRequiredData =
            (inputs.sameSiteOnly() && !siteKnown)
            || (inputs.requireRatio() && !ratioKnown)
            || (inputs.requireVectorGroup() && !vectorKnown)
            || (inputs.requireTapConfiguration() && !tapKnown)
            || (inputs.ratedPowerRatio() && !powerKnown)
            || (inputs.requireImpedance() && !impedanceKnown);
        String verdict = hardFailure
            ? "Not compatible"
            : (unknownRequiredData
                || (maxUkDiff != null && maxUkDiff > inputs.ukLimit() * 0.8) ? "Review" : "Compatible");

        String ratio = !inputs.requireRatio() && !ratioOk
            ? ratioText(ratioA) + " / " + ratioText(ratioB)
            : ratioText(ratioA);
        String vector = !inputs.requireVectorGroup() && !vectorOk
            ? vectorA + " / " + vectorB
            : (vectorA.isEmpty() ? "—" : vectorA);
        String tapRange = !inputs.requireTapConfiguration()
            ? tapScheduleText(tapsA, ratioA)
            : (!tapKnown ? "Missing tap data"
                : (tapOk ? tapScheduleText(tapsA, ratioA) : "Different range"));

        return new CompatibilityResult(
            candidateA.name(), candidateB.name(),
            cleanText(candidateA.siteName()).isEmpty() ? "—" : candidateA.siteName(),
            ratio, vector, tapRange,
            maxUkDiff == null ? null : roundOneDecimal(maxUkDiff), verdict);
    }

    // ─── Transformer Fleet (with filters) ─────────────────────────────────

    public TransformerFleetSummary getTransformerFleetSummary(String org, String mfr, String voltage, String status,
                                                               String city, String ward, String substation) {
        Long orgId = parseId(org);
        Long mfrId = parseId(mfr);
        Integer volt = parseVoltage(voltage);
        Long sts = parseStatus(status);
        String c = emptyToNull(city);
        String w = emptyToNull(ward);
        Long subId = parseId(substation);

        long totalAssets = assetInfoRepo.countActiveFiltered(orgId, mfrId, volt, c, w, null);
        long totalSubstations = substationRepo.countActive();
        long totalTransformers = transformerRepo.countActiveFiltered(orgId, mfrId, volt, sts, c, w, subId, null);
        double totalCapacityKVA = transformerRepo.sumRatedPowerFiltered(orgId, mfrId, volt, sts, c, w, subId, null);
        double totalCapacityMVA = Math.round(totalCapacityKVA / 10.0) / 100.0;

        long nameplateCount = transformerRepo.countWithRatedPowerFiltered(orgId, mfrId, volt, sts, c, w, subId, null);
        double nameplatePct = totalTransformers > 0
            ? Math.round((double) nameplateCount / totalTransformers * 1000.0) / 10.0
            : 0;

        double averageFleetAge = 0;
        if (lstSize(transformerRepo.averageOperationDateYearFiltered(orgId, mfrId, volt, sts, c, w, subId, null)) > 0) {
            Object[] row = transformerRepo.averageOperationDateYearFiltered(orgId, mfrId, volt, sts, c, w, subId, null).get(0);
            if (row[0] != null) {
                double avgYear = ((Number) row[0]).doubleValue();
                int currentYear = java.time.Year.now().getValue();
                averageFleetAge = Math.max(0, Math.round((currentYear - avgYear) * 10.0) / 10.0);
            }
        }

        return new TransformerFleetSummary(totalAssets, totalSubstations, totalTransformers, totalCapacityMVA,
            nameplateCount, nameplatePct, averageFleetAge);
    }

    private static int lstSize(java.util.List<?> list) {
        return list == null ? 0 : list.size();
    }

    public List<VoltageCapacityItem> getCapacityByVoltage(String org, String mfr, String voltage, String status,
                                                           String city, String ward, String substation) {
        Long orgId = parseId(org);
        Long mfrId = parseId(mfr);
        Integer volt = parseVoltage(voltage);
        Long sts = parseStatus(status);
        String c = emptyToNull(city);
        String w = emptyToNull(ward);
        Long subId = parseId(substation);

        List<Object[]> rows = transformerRepo.capacityByVoltageLevel(orgId, mfrId, volt, sts, c, w, subId, null);
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
                                                         String city, String ward, String substation) {
        Long orgId = parseId(org);
        Long mfrId = parseId(mfr);
        Integer volt = parseVoltage(voltage);
        Long sts = parseStatus(status);
        String c = emptyToNull(city);
        String w = emptyToNull(ward);
        Long subId = parseId(substation);

        List<Object[]> rows = transformerRepo.countByOrganisation(orgId, mfrId, volt, sts, c, w, subId, null);
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
                                                String city, String ward, String substation) {
        Long orgId = parseId(org);
        Long mfrId = parseId(mfr);
        Integer volt = parseVoltage(voltage);
        Long sts = parseStatus(status);
        String c = emptyToNull(city);
        String w = emptyToNull(ward);
        Long subId = parseId(substation);

          List<Object[]> rows = transformerRepo.countByManufacturer(orgId, mfrId, volt, sts, c, w, subId, null);
          long total = rows.stream().mapToLong(r -> ((Number) r[1]).longValue()).sum();
          if (total == 0) return List.of();
          List<ChartItem> items = new ArrayList<>();
          long accounted = 0;
          int i = 0;
          for (Object[] r : rows) {
              String name = r[0] != null ? (String) r[0] : "Unknown";
              long count = ((Number) r[1]).longValue();
              if (i < 4) {
                  double pct = Math.round((double) count / total * 1000.0) / 10.0;
                  items.add(new ChartItem(name, pct, count));
                  accounted += count;
              } else {
                  break;
              }
              i++;
          }
          long othersCount = total - accounted;
          if (othersCount > 0) {
              double othersPct = Math.round((double) othersCount / total * 1000.0) / 10.0;
              items.add(new ChartItem("Others", othersPct, othersCount));
          }
          return items;
      }

    public List<DataCompletenessItem> getDataCompleteness(String org, String mfr, String voltage, String status,
                                                           String city, String ward, String assetClass) {
        Long orgId = parseId(org);
        Long mfrId = parseId(mfr);
        Integer volt = parseVoltage(voltage);
        String c = emptyToNull(city);
        String w = emptyToNull(ward);
        String ac = emptyToNull(assetClass);

        long total = assetInfoRepo.countActiveFiltered(orgId, mfrId, volt, c, w, ac);
        if (total == 0) return List.of();
        long missingMfr = assetInfoRepo.countMissingManufacturerFiltered(orgId, mfrId, volt, c, w, ac);
        long missingYear = assetInfoRepo.countMissingManufacturingYearFiltered(orgId, mfrId, volt, c, w, ac);
        long missingSerial = assetInfoRepo.countMissingSerialNoFiltered(orgId, mfrId, volt, c, w, ac);
        long missingCountry = assetInfoRepo.countMissingCountryOfOriginFiltered(orgId, mfrId, volt, c, w, ac);
        return List.of(
            new DataCompletenessItem("Manufacturer", "Properties", missingMfr, Math.round((double) missingMfr / total * 1000.0) / 10.0, total),
            new DataCompletenessItem("Manufacturing Year", "Properties", missingYear, Math.round((double) missingYear / total * 1000.0) / 10.0, total),
            new DataCompletenessItem("Serial Number", "Properties", missingSerial, Math.round((double) missingSerial / total * 1000.0) / 10.0, total),
            new DataCompletenessItem("Country of Origin", "Properties", missingCountry, Math.round((double) missingCountry / total * 1000.0) / 10.0, total)
        );
    }

    // ─── Asset & Capacity Overview (Dashboard 1) ────────────────────────────

    public TransformerFleetSummary getOverviewSummary(String org, String city, String ward, String voltage,
                                                      String assetClass, String status) {
        Long orgId = parseId(org);
        Integer volt = parseVoltage(voltage);
        Long sts = parseStatus(status);
        String c = emptyToNull(city);
        String w = emptyToNull(ward);
        String ac = emptyToNull(assetClass);

        long totalAssets = assetInfoRepo.countActiveFiltered(orgId, null, volt, c, w, ac);
        long totalSubstations = transformerRepo.countSubstationsFiltered(orgId, null, volt, sts, c, w, null, ac);
        long totalTransformers = transformerRepo.countActiveFiltered(orgId, null, volt, sts, c, w, null, ac);
        double totalCapacityKVA = transformerRepo.sumRatedPowerFiltered(orgId, null, volt, sts, c, w, null, ac);
        double totalCapacityMVA = Math.round(totalCapacityKVA / 10.0) / 100.0;
        return new TransformerFleetSummary(totalAssets, totalSubstations, totalTransformers, totalCapacityMVA,
            0, 0, 0);
    }

    private static String overviewVoltageBucket(int kv) {
        if (kv == 500) return "500kV";
        if (kv == 220) return "220kV";
        if (kv == 110 || kv == 115) return "110kV";
        return "Distribution";
    }

    public List<VoltageCapacityItem> getOverviewCapacityByVoltage(String org, String city, String ward,
                                                                  String voltage, String assetClass, String status) {
        Long orgId = parseId(org);
        Integer volt = parseVoltage(voltage);
        Long sts = parseStatus(status);
        String c = emptyToNull(city);
        String w = emptyToNull(ward);
        String ac = emptyToNull(assetClass);

        List<Object[]> rows = transformerRepo.capacityByVoltageLevel(orgId, null, volt, sts, c, w, null, ac);
        java.util.Map<String, double[]> buckets = new java.util.LinkedHashMap<>();
        for (String label : List.of("500kV", "220kV", "110kV", "Distribution")) {
            buckets.put(label, new double[]{0, 0});
        }
        for (Object[] r : rows) {
            int kv = ((Number) r[0]).intValue();
            double kva = ((Number) r[1]).doubleValue();
            long count = ((Number) r[2]).longValue();
            double[] bucket = buckets.get(overviewVoltageBucket(kv));
            bucket[0] += kva;
            bucket[1] += count;
        }
        double maxCapacity = buckets.values().stream().mapToDouble(b -> b[0]).max().orElse(0);
        List<VoltageCapacityItem> items = new ArrayList<>();
        for (java.util.Map.Entry<String, double[]> e : buckets.entrySet()) {
            if (e.getValue()[1] == 0) continue;
            double mva = Math.round(e.getValue()[0] / 10.0) / 100.0;
            double heightPct = maxCapacity > 0 ? e.getValue()[0] / maxCapacity * 100 : 0;
            items.add(new VoltageCapacityItem(e.getKey(), mva, (long) e.getValue()[1],
                Math.round(heightPct * 10.0) / 10.0));
        }
        return items;
    }

    public List<AssetMixItem> getOverviewMixByOrganisation(String org, String city, String ward, String voltage,
                                                           String assetClass, String status) {
        Long orgId = parseId(org);
        Integer volt = parseVoltage(voltage);
        Long sts = parseStatus(status);
        String c = emptyToNull(city);
        String w = emptyToNull(ward);
        String ac = emptyToNull(assetClass);

        List<Object[]> rows = transformerRepo.countByOrganisation(orgId, null, volt, sts, c, w, null, ac);
        long total = rows.stream().mapToLong(r -> ((Number) r[1]).longValue()).sum();
        if (total == 0) return List.of();
        List<AssetMixItem> items = new ArrayList<>();
        long accounted = 0;
        int i = 0;
        for (Object[] r : rows) {
            String name = r[0] != null ? (String) r[0] : "Unknown";
            long count = ((Number) r[1]).longValue();
            if (i < 4) {
                double pct = Math.round((double) count / total * 1000.0) / 10.0;
                items.add(new AssetMixItem(name, count, pct));
                accounted += count;
            } else {
                break;
            }
            i++;
        }
        long othersCount = total - accounted;
        if (othersCount > 0) {
            double othersPct = Math.round((double) othersCount / total * 1000.0) / 10.0;
            items.add(new AssetMixItem("Others", othersCount, othersPct));
        }
        return items;
    }

    public List<ChartItem> getOverviewTopManufacturers(String org, String city, String ward, String voltage,
                                                       String assetClass, String status) {
        Long orgId = parseId(org);
        Integer volt = parseVoltage(voltage);
        Long sts = parseStatus(status);
        String c = emptyToNull(city);
        String w = emptyToNull(ward);
        String ac = emptyToNull(assetClass);

        List<Object[]> rows = transformerRepo.countByManufacturer(orgId, null, volt, sts, c, w, null, ac);
        long total = rows.stream().mapToLong(r -> ((Number) r[1]).longValue()).sum();
        if (total == 0) return List.of();
        return rows.stream().limit(5).map(r -> {
            String name = r[0] != null ? (String) r[0] : "Unknown";
            long count = ((Number) r[1]).longValue();
            double pct = Math.round((double) count / total * 1000.0) / 10.0;
            return new ChartItem(name, pct, count);
        }).collect(Collectors.toList());
    }

    public List<CompatibilityResult> getCompatibilityResults(String org, String mfr, String voltage, String status,
                                                               boolean sameSiteOnly, boolean requireRatio,
                                                               boolean requireVectorGroup, boolean requireTapConfiguration,
                                                               boolean requireImpedance, boolean ratedPowerRatio,
                                                               double ukLimit, String city, String ward,
                                                               String substation) {
        Long orgId = parseId(org);
        Long mfrId = parseId(mfr);
        Integer volt = parseVoltage(voltage);
        Long sts = parseStatus(status);
        String c = emptyToNull(city);
        String w = emptyToNull(ward);
        Long subId = parseId(substation);
        double maxUk = Math.max(0, ukLimit);

        List<Object[]> details = transformerRepo.findTransformerDetailsForCompatibility(
            orgId, mfrId, volt, sts, c, w, subId, null);
        if (details.isEmpty()) return List.of();

        Map<Long, ParallelCandidate> transformers = new LinkedHashMap<>();
        for (Object[] row : details) {
            long id = ((Number) row[0]).longValue();
            String vectorGroup = cleanText(row[1]);
            String assetName = cleanText(row[4]);
            if (assetName.isEmpty()) assetName = cleanText(row[5]);
            Long siteId = row[6] instanceof Number number ? number.longValue() : null;
            String siteName = cleanText(row[7]);
            transformers.put(id, new ParallelCandidate(
                id, assetName.isEmpty() ? "T-" + id : assetName,
                vectorGroup, siteId, siteName));
        }
        List<Long> ids = new ArrayList<>(transformers.keySet());

        Map<Long, Map<String, Double>> ratios = new HashMap<>();
        for (Object[] row : transformerRepo.findVoltageRatingsByTransformerIds(ids)) {
            long id = ((Number) row[0]).longValue();
            Double kv = toKilovolts(row[2], row[3]);
            if (kv == null || kv <= 0) continue;
            ratios.computeIfAbsent(id, key -> new TreeMap<>()).put(normalizeWinding(row[1]), kv);
        }

        Map<Long, List<Object[]>> changerRows = new HashMap<>();
        for (Object[] row : transformerRepo.findTapChangersByTransformerIds(ids)) {
            long id = ((Number) row[1]).longValue();
            changerRows.computeIfAbsent(id, key -> new ArrayList<>()).add(row);
        }
        Map<Long, List<Long>> changerIds = new HashMap<>();
        for (Map.Entry<Long, List<Object[]>> entry : changerRows.entrySet()) {
            List<Long> changerIdList = new ArrayList<>();
            for (Object[] row : entry.getValue()) changerIdList.add(((Number) row[0]).longValue());
            changerIds.put(entry.getKey(), changerIdList);
        }
        List<Long> allChangerIds = changerIds.values().stream().flatMap(List::stream).distinct().toList();
        Map<Long, List<TapPoint>> changerSchedules = new HashMap<>();
        if (!allChangerIds.isEmpty()) {
            for (Object[] row : transformerRepo.findTapChangerVoltagesByChangerIds(allChangerIds)) {
                long changerId = ((Number) row[0]).longValue();
                Integer position = row[1] instanceof Number number ? number.intValue() : null;
                Double kv = toKilovolts(row[2], row[3]);
                if (kv == null || kv <= 0) continue;
                changerSchedules.computeIfAbsent(changerId, key -> new ArrayList<>())
                    .add(new TapPoint(position, kv));
            }
            changerSchedules.values().forEach(schedule -> schedule.sort(
                Comparator.comparing(TapPoint::position, Comparator.nullsLast(Integer::compareTo))
                    .thenComparing(TapPoint::kv)));
        }
        Map<Long, List<TapProfile>> taps = new HashMap<>();
        for (Map.Entry<Long, List<Object[]>> entry : changerRows.entrySet()) {
            List<TapProfile> profiles = new ArrayList<>();
            for (Object[] row : entry.getValue()) {
                long changerId = ((Number) row[0]).longValue();
                Integer tapCount = row[6] instanceof Number number ? number.intValue() : null;
                profiles.add(new TapProfile(
                    normalizeWinding(row[3]), normalizeTapType(row[2]), tapCount, cleanText(row[4]),
                    changerSchedules.getOrDefault(changerId, List.of())));
            }
            taps.put(entry.getKey(), profiles);
        }

        Map<Long, Map<String, List<Double>>> impedances = new HashMap<>();
        for (Object[] row : transformerRepo.findImpedancesByTransformerIds(ids)) {
            long id = ((Number) row[0]).longValue();
            if (!(row[2] instanceof Number number) || !isPercentUnit(row[3])) continue;
            String type = cleanText(row[1]).toUpperCase(java.util.Locale.ROOT);
            impedances.computeIfAbsent(id, key -> new HashMap<>())
                .computeIfAbsent(type.isEmpty() ? "UNKNOWN" : type, key -> new ArrayList<>())
                .add(number.doubleValue());
        }

        Map<Long, List<Double>> ratedPowers = new HashMap<>();
        for (Object[] row : transformerRepo.findPowerRatingsByTransformerIds(ids)) {
            long id = ((Number) row[0]).longValue();
            Double kva = toKva(row[1], row[2]);
            if (kva == null || kva <= 0) continue;
            ratedPowers.computeIfAbsent(id, key -> new ArrayList<>()).add(kva);
        }

        ParallelScreeningInputs inputs = new ParallelScreeningInputs(
            transformers, ratios, taps, impedances, ratedPowers,
            sameSiteOnly, requireRatio, requireVectorGroup, requireTapConfiguration,
            requireImpedance, ratedPowerRatio, maxUk);

        Map<String, List<Long>> buckets = new LinkedHashMap<>();
        for (Long id : ids) {
            ParallelCandidate candidate = transformers.get(id);
            String siteKey = candidate.siteId() == null
                ? "UNKNOWN-" + cleanText(candidate.siteName())
                : candidate.siteId().toString();
            String bucket = (sameSiteOnly ? "SITE:" + siteKey : "SITE:*")
                + "|RATIO:" + (requireRatio ? ratioSignature(ratios.getOrDefault(id, Map.of())) : "*")
                + "|VECTOR:" + (requireVectorGroup ? cleanText(candidate.vectorGroup()) : "*");
            buckets.computeIfAbsent(bucket, key -> new ArrayList<>()).add(id);
        }

        List<CompatibilityResult> results = new ArrayList<>();
        int maxResults = 200;
        pairs:
        for (List<Long> bucket : buckets.values()) {
            for (int i = 0; i < bucket.size() && results.size() < maxResults; i++) {
                for (int j = i + 1; j < bucket.size() && results.size() < maxResults; j++) {
                    results.add(scoreParallelPair(bucket.get(i), bucket.get(j), inputs));
                }
            }
            if (results.size() >= maxResults) break pairs;
        }

        Map<String, Integer> rank = Map.of("Compatible", 0, "Review", 1, "Not compatible", 2);
        results.sort(Comparator
            .comparing((CompatibilityResult result) -> rank.getOrDefault(result.result(), 3))
            .thenComparing(result -> result.ukDiff() == null)
            .thenComparing(result -> result.ukDiff() == null ? Double.MAX_VALUE : result.ukDiff())
            .thenComparing(CompatibilityResult::transformerA)
            .thenComparing(CompatibilityResult::transformerB));
        return results.size() > maxResults ? results.subList(0, maxResults) : results;
    }

    // ─── Data Quality (with filters) ────────────────────────────────────────

    private record PhaseBalance(long monitored, long groups, long balanced) {
        double balancedPct() {
            return groups == 0 ? 0 : roundOneDecimal((double) balanced / groups * 100.0);
        }

        long mismatches() {
            return Math.max(0, groups - balanced);
        }
    }

    private static String normalizeQualityAssetClass(String assetClass) {
        String value = assetClass == null ? "" : assetClass.trim().toLowerCase(java.util.Locale.ROOT);
        return switch (value) {
            case "", "all" -> null;
            case "transformer", "circuit-breaker", "ct", "vt", "disconnector" -> value;
            default -> null;
        };
    }

    private static String normalizeQualityStatus(String qualityStatus) {
        String value = qualityStatus == null ? "" : qualityStatus.trim().toLowerCase(java.util.Locale.ROOT);
        return switch (value) {
            case "", "all" -> null;
            case "complete", "incomplete", "invalid" -> value;
            default -> null;
        };
    }

    private static String normalizeQualityCategory(String category) {
        String value = category == null ? "" : category.trim().toLowerCase(java.util.Locale.ROOT);
        return switch (value) {
            case "", "all" -> null;
            case "basic" -> "basic-specification";
            case "properties", "nameplate", "basic-specification", "ratings", "others" -> value;
            default -> null;
        };
    }

    private static String qualityCategoryLabel(String category) {
        return switch (category) {
            case "properties" -> "Properties";
            case "nameplate" -> "Nameplate";
            case "basic-specification" -> "Basic Specification";
            case "ratings" -> "Ratings";
            default -> "Others";
        };
    }

    private static long countAt(Object[] row, int index) {
        if (row == null || index >= row.length || !(row[index] instanceof Number number)) return 0;
        return number.longValue();
    }

    private static double fieldCompleteness(long complete, long total) {
        if (total <= 0) return 0;
        return roundOneDecimal((double) complete / total * 100.0);
    }

    private static double averageQualityScores(java.util.Collection<Double> scores) {
        if (scores == null || scores.isEmpty()) return 0;
        double total = 0;
        for (Double score : scores) total += score == null ? 0 : score;
        return roundOneDecimal(total / scores.size());
    }

    private static java.util.Map<String, Double> qualityCategoryScores(Object[] counts, long total) {
        java.util.Map<String, Double> scores = new java.util.LinkedHashMap<>();
        long missingSerial = countAt(counts, 1);
        long missingAssetName = countAt(counts, 2);
        long missingManufacturer = countAt(counts, 3);
        long missingOperationDate = countAt(counts, 4);
        long missingYear = countAt(counts, 5);
        long missingCountry = countAt(counts, 6);
        long missingApparatus = countAt(counts, 7);
        scores.put("properties", roundOneDecimal(
            (fieldCompleteness(total - missingSerial, total)
                + fieldCompleteness(total - missingManufacturer, total)
                + fieldCompleteness(total - missingOperationDate, total)) / 3.0));
        scores.put("nameplate", fieldCompleteness(total - missingAssetName, total));
        scores.put("basic-specification", roundOneDecimal(
            (fieldCompleteness(total - missingManufacturer, total)
                + fieldCompleteness(total - missingYear, total)) / 2.0));
        scores.put("ratings", fieldCompleteness(total - missingCountry, total));
        scores.put("others", fieldCompleteness(total - missingApparatus, total));
        return scores;
    }

    private static java.util.Map<String, Long> qualityCategoryMissing(Object[] counts) {
        java.util.Map<String, Long> missing = new java.util.LinkedHashMap<>();
        missing.put("properties", countAt(counts, 8));
        missing.put("nameplate", countAt(counts, 2));
        missing.put("basic-specification", countAt(counts, 9));
        missing.put("ratings", countAt(counts, 6));
        missing.put("others", countAt(counts, 7));
        return missing;
    }

    private PhaseBalance getQualityPhaseBalance(Long orgId, Long mfrId, Integer volt, String city,
                                                String assetClass, String qualityStatus) {
        List<Object[]> rows = assetInfoRepo.findQualityPhaseGroups(
            orgId, mfrId, volt, city, assetClass, qualityStatus);
        long monitored = 0;
        long groups = 0;
        long balanced = 0;
        for (Object[] row : rows) {
            long phaseA = countAt(row, 2);
            long phaseB = countAt(row, 3);
            long phaseC = countAt(row, 4);
            monitored += phaseA + phaseB + phaseC;
            groups++;
            if (phaseA > 0 && phaseA == phaseB && phaseB == phaseC) balanced++;
        }
        return new PhaseBalance(monitored, groups, balanced);
    }

    private static PhaseCount phaseBalanceRow(String assetClass, long phaseA, long phaseB, long phaseC) {
        long total = phaseA + phaseB + phaseC;
        if (total == 0) return new PhaseCount(assetClass, 0, 0, 0, "No data", "blue");
        long difference = Math.max(phaseA, Math.max(phaseB, phaseC))
            - Math.min(phaseA, Math.min(phaseB, phaseC));
        if (difference == 0) return new PhaseCount(assetClass, phaseA, phaseB, phaseC, "Balanced", "green");
        if (difference <= 2) return new PhaseCount(assetClass, phaseA, phaseB, phaseC, "-" + difference + " C", "amber");
        return new PhaseCount(assetClass, phaseA, phaseB, phaseC, "Mismatch", "red");
    }

    public DataQualitySummary getDataQualitySummary(String org, String mfr, String voltage, String region,
                                                     String assetClass, String category, String qualityStatus) {
        Long orgId = parseId(org);
        Long mfrId = parseId(mfr);
        Integer volt = parseVoltage(voltage);
        String city = emptyToNull(region);
        String asset = normalizeQualityAssetClass(assetClass);
        String selectedCategory = normalizeQualityCategory(category);
        String status = normalizeQualityStatus(qualityStatus);

        List<Object[]> rows = assetInfoRepo.findQualityFieldCounts(orgId, mfrId, volt, city, asset, status);
        Object[] counts = rows.isEmpty() ? null : rows.get(0);
        long total = countAt(counts, 0);
        java.util.Map<String, Double> scores = qualityCategoryScores(counts, total);
        java.util.Map<String, Long> missingByCategory = qualityCategoryMissing(counts);
        double overall = selectedCategory == null
            ? averageQualityScores(scores.values())
            : scores.getOrDefault(selectedCategory, 0.0);
        long missingFields = selectedCategory == null
            ? countAt(counts, 10)
            : missingByCategory.getOrDefault(selectedCategory, 0L);
        long missingNameplates = countAt(counts, 2);
        PhaseBalance balance = getQualityPhaseBalance(orgId, mfrId, volt, city, asset, status);

        return new DataQualitySummary(overall, missingFields, missingNameplates,
            balance.monitored(), balance.balancedPct(), balance.mismatches());
    }

    public List<ChartItem> getCompletenessByCategory(String org, String mfr, String voltage, String region,
                                                      String assetClass, String category, String qualityStatus) {
        Long orgId = parseId(org);
        Long mfrId = parseId(mfr);
        Integer volt = parseVoltage(voltage);
        String city = emptyToNull(region);
        String asset = normalizeQualityAssetClass(assetClass);
        String selectedCategory = normalizeQualityCategory(category);
        String status = normalizeQualityStatus(qualityStatus);

        List<Object[]> rows = assetInfoRepo.findQualityFieldCounts(orgId, mfrId, volt, city, asset, status);
        if (rows.isEmpty()) return List.of();
        Object[] counts = rows.get(0);
        long total = countAt(counts, 0);
        if (total == 0) return List.of();
        java.util.Map<String, Double> scores = qualityCategoryScores(counts, total);
        if (selectedCategory != null) {
            return List.of(new ChartItem(qualityCategoryLabel(selectedCategory),
                scores.getOrDefault(selectedCategory, 0.0), 0));
        }
        return List.of(
            new ChartItem("Properties", scores.getOrDefault("properties", 0.0), 0),
            new ChartItem("Nameplate", scores.getOrDefault("nameplate", 0.0), 0),
            new ChartItem("Basic Specification", scores.getOrDefault("basic-specification", 0.0), 0),
            new ChartItem("Ratings", scores.getOrDefault("ratings", 0.0), 0),
            new ChartItem("Others", scores.getOrDefault("others", 0.0), 0)
        );
    }

    public List<ChartItem> getQualityStatusDistribution(String org, String mfr, String voltage, String region,
                                                        String assetClass) {
        Long orgId = parseId(org);
        Long mfrId = parseId(mfr);
        Integer volt = parseVoltage(voltage);
        String city = emptyToNull(region);
        String asset = normalizeQualityAssetClass(assetClass);

        List<Object[]> rows = assetInfoRepo.findQualityFieldCounts(orgId, mfrId, volt, city, asset, null);
        if (rows.isEmpty()) return List.of();
        Object[] counts = rows.get(0);
        long total = countAt(counts, 0);
        if (total == 0) return List.of();
        double completePct = roundOneDecimal((double) countAt(counts, 11) / total * 100.0);
        double incompletePct = roundOneDecimal((double) countAt(counts, 13) / total * 100.0);
        double invalidPct = Math.max(0, roundOneDecimal(100.0 - completePct - incompletePct));

        return List.of(
            new ChartItem("Complete", completePct, 0),
            new ChartItem("Incomplete", incompletePct, 0),
            new ChartItem("Invalid", invalidPct, 0)
        );
    }

    public List<PhaseCount> getAssetCountByPhase(String org, String mfr, String voltage, String region,
                                                String assetClass, String qualityStatus) {
        Long orgId = parseId(org);
        Long mfrId = parseId(mfr);
        Integer volt = parseVoltage(voltage);
        String city = emptyToNull(region);
        String asset = normalizeQualityAssetClass(assetClass);
        String status = normalizeQualityStatus(qualityStatus);
        if (asset != null && !asset.equals("ct") && !asset.equals("vt")) return List.of();

        java.util.Map<String, long[]> totals = new java.util.LinkedHashMap<>();
        totals.put("Current Transformer", new long[3]);
        totals.put("Voltage Transformer", new long[3]);
        totals.put("Surge Arrester", new long[3]);
        totals.put("Bushing", new long[3]);
        for (Object[] row : assetInfoRepo.findQualityPhaseGroups(orgId, mfrId, volt, city, asset, status)) {
            long[] total = totals.get(String.valueOf(row[0]));
            if (total == null) continue;
            total[0] += countAt(row, 2);
            total[1] += countAt(row, 3);
            total[2] += countAt(row, 4);
        }

        List<PhaseCount> result = new ArrayList<>();
        if (asset == null || asset.equals("ct")) {
            long[] total = totals.get("Current Transformer");
            result.add(phaseBalanceRow("Current Transformer", total[0], total[1], total[2]));
        }
        if (asset == null || asset.equals("vt")) {
            long[] total = totals.get("Voltage Transformer");
            result.add(phaseBalanceRow("Voltage Transformer", total[0], total[1], total[2]));
        }
        if (asset == null) {
            for (String assetName : List.of("Surge Arrester", "Bushing")) {
                long[] total = totals.get(assetName);
                result.add(phaseBalanceRow(assetName, total[0], total[1], total[2]));
            }
        }
        return result;
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

        long totalTransformers = transformerRepo.countActiveFiltered(orgId, null, null, null, c, w, null, null);
        long totalSubstations = substationRepo.countActive();
        long totalCircuitBreakers = circuitBreakerRepo.countAll();
        long totalDisconnectors = disconnectorRepo.countAll();
        long totalCTs = ctRepo.countAll();
        long totalVTs = vtRepo.countAll();
        long totalAssets = totalTransformers + totalCircuitBreakers + totalDisconnectors + totalCTs + totalVTs;
        double totalCapacityKVA = transformerRepo.sumRatedPowerFiltered(orgId, null, null, null, c, w, null, null);
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
        List<Object[]> rows = transformerRepo.capacityByVoltageLevel(orgId, null, null, null, c, null, null, null);
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
        long total = assetInfoRepo.countActiveFiltered(orgId, null, null, null, null, null);
        if (total == 0) {
            return List.of(new ChartItem("No data", 100, 0));
        }
        long missingSerial = assetInfoRepo.countMissingSerialNoFiltered(orgId, null, null, null, null, null);
        long missingMfr = assetInfoRepo.countMissingManufacturerFiltered(orgId, null, null, null, null, null);
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
        long tx = transformerRepo.countActiveFiltered(orgId, null, null, null, null, null, null, null);
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
        long totalAssets = assetInfoRepo.countActiveFiltered(orgId, null, null, null, null, null);
        if (totalAssets == 0) {
            return List.of(
                new KeyValueItem("Equipment hierarchy", 0),
                new KeyValueItem("Equipment nameplate", 0),
                new KeyValueItem("Operating data", 0),
                new KeyValueItem("Maintenance data", 0)
            );
        }
        long missingSerial = assetInfoRepo.countMissingSerialNoFiltered(orgId, null, null, null, null, null);
        long missingMfr = assetInfoRepo.countMissingManufacturerFiltered(orgId, null, null, null, null, null);
        long missingYear = assetInfoRepo.countMissingManufacturingYearFiltered(orgId, null, null, null, null, null);
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
