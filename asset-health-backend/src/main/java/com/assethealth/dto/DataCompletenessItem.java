package com.assethealth.dto;

public record DataCompletenessItem(
    String fieldName,
    String category,
    long missingCount,
    double missingPct,
    long total
) {}
