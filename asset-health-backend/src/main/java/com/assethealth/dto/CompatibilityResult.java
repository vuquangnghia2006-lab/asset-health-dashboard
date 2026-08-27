package com.assethealth.dto;

public record CompatibilityResult(
    String transformerA,
    String transformerB,
    String site,
    String ratio,
    String vectorGroup,
    String tapRange,
    double ukDiff
) {}
