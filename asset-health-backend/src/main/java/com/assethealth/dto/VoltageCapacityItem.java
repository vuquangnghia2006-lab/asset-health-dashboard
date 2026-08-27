package com.assethealth.dto;

public record VoltageCapacityItem(
    String label,
    double capacityMVA,
    long count,
    double heightPct
) {}
