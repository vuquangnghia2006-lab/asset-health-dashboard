package com.assethealth.dto;

public record PlantSummary(
    long totalPlants,
    String totalCapacity,
    long totalUnits,
    long criticalAssets,
    long overdueTests
) {}
