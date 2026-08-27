package com.assethealth.dto;

public record TransformerFleetSummary(
    long totalAssets,
    long totalSubstations,
    long totalTransformers,
    double totalCapacityMVA
) {}
