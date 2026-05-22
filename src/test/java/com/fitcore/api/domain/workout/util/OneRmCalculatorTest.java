package com.fitcore.api.domain.workout.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class OneRmCalculatorTest {

    private static final double DELTA = 0.01;

    @Test
    @DisplayName("1RM reps=1 이면 weight 그대로 (1 + 1/30 ≈ 1.033)")
    void oneRep() {
        double result = OneRmCalculator.estimate(100.0, 1);
        assertThat(result).isCloseTo(103.33, within(DELTA));
    }

    @Test
    @DisplayName("표준 케이스: 100kg × 10회 = 133.33")
    void standardCase() {
        double result = OneRmCalculator.estimate(100.0, 10);
        assertThat(result).isCloseTo(133.33, within(DELTA));
    }

    @Test
    @DisplayName("80kg × 5회 = 93.33")
    void commonStrengthSet() {
        double result = OneRmCalculator.estimate(80.0, 5);
        assertThat(result).isCloseTo(93.33, within(DELTA));
    }

    @Test
    @DisplayName("중량 0이면 결과도 0")
    void zeroWeight() {
        assertThat(OneRmCalculator.estimate(0.0, 10)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("무거운 고중량 저반복 케이스: 200kg × 3회 = 220.0")
    void heavyLowRep() {
        double result = OneRmCalculator.estimate(200.0, 3);
        assertThat(result).isCloseTo(220.0, within(DELTA));
    }

    @Test
    @DisplayName("동일 운동 두 세트 중 높은 1RM이 더 큰지 검증 (PR 선택 로직 대리 테스트)")
    void higherEstimateWins() {
        double set1 = OneRmCalculator.estimate(100.0, 5);  // 116.67
        double set2 = OneRmCalculator.estimate(95.0, 8);   // 120.33
        assertThat(set2).isGreaterThan(set1);
    }
}
