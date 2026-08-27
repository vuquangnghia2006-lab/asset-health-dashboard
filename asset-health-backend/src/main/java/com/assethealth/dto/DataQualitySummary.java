package com.assethealth.dto;

public record DataQualitySummary(
    double overallCompleteness,
    long missingFields,
    long missingNameplates,
    long phaseMonitored,
    double balancedGroups,
    long phaseMismatches
) {}
