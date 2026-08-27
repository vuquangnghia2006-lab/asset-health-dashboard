package com.assethealth.dto;

public record TransformerFleetOverview(
    long totalAssets,
    long totalSubstations,
    long totalTransformers,
    double transformerCapacityMVA
) {}
