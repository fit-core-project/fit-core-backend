package com.fitcore.api.domain.workout.util;

public final class OneRmCalculator {

    private OneRmCalculator() {}

    /**
     * Epley formula: weight × (1 + reps / 30)
     */
    public static double estimate(double weightKg, int reps) {
        return weightKg * (1.0 + reps / 30.0);
    }
}
