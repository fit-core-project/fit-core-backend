package com.fitcore.api.domain.nutrition;

import com.fitcore.api.domain.nutrition.repository.DietLogRepository;
import com.fitcore.api.domain.nutrition.response.DietDailyAggregationResponse;
import com.fitcore.api.domain.nutrition.service.DietLogService;
import com.fitcore.api.global.common.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DietLogDailyAggregationServiceTest {

    @Mock DietLogRepository dietLogRepository;
    @Mock SecurityUtils securityUtils;
    @InjectMocks DietLogService dietLogService;

    @BeforeEach
    void setup() {
        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
    }

    @Test
    void getDailyAggregation_delegatesWithUserId() {
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 7);

        DietDailyAggregationResponse stubRow = new DietDailyAggregationResponse(
                LocalDate.of(2026, 6, 3),
                500L,
                BigDecimal.valueOf(60.0), BigDecimal.valueOf(30.0), BigDecimal.valueOf(15.0),
                2L);

        when(dietLogRepository.findDailyAggregation("user-1", from, to))
                .thenReturn(List.of(stubRow));

        List<DietDailyAggregationResponse> result = dietLogService.getDailyAggregation(from, to);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDate()).isEqualTo(LocalDate.of(2026, 6, 3));
        assertThat(result.get(0).getTotalKcal()).isEqualTo(500);
        assertThat(result.get(0).getTotalCarbsG()).isEqualByComparingTo("60.0");
        assertThat(result.get(0).getTotalProteinG()).isEqualByComparingTo("30.0");
        assertThat(result.get(0).getTotalFatG()).isEqualByComparingTo("15.0");
        assertThat(result.get(0).getCount()).isEqualTo(2L);
    }

    @Test
    void getDailyAggregation_emptyRange_returnsEmptyList() {
        when(dietLogRepository.findDailyAggregation(any(), any(), any()))
                .thenReturn(List.of());

        List<DietDailyAggregationResponse> result = dietLogService.getDailyAggregation(
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 1));
        assertThat(result).isEmpty();
    }

    @Test
    void response_nullMacros_defaultToZero() {
        DietDailyAggregationResponse row = new DietDailyAggregationResponse(
                LocalDate.of(2026, 6, 5), 200L, null, null, null, 1L);

        assertThat(row.getTotalCarbsG()).isEqualByComparingTo("0.0");
        assertThat(row.getTotalProteinG()).isEqualByComparingTo("0.0");
        assertThat(row.getTotalFatG()).isEqualByComparingTo("0.0");
    }
}
