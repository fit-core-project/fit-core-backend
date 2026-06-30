package com.fitcore.api.domain.nutrition;

import com.fitcore.api.domain.nutrition.response.DietDailyAggregationResponse;
import com.fitcore.api.domain.nutrition.service.DietLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class DietLogDailyAggregationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DietLogService dietLogService;

    @Test
    void getDailySummary_returnsAggregatedList() throws Exception {
        DietDailyAggregationResponse row = new DietDailyAggregationResponse(
                LocalDate.of(2026, 6, 10),
                800L,
                BigDecimal.valueOf(100.0), BigDecimal.valueOf(50.0), BigDecimal.valueOf(20.0),
                3L);

        when(dietLogService.getDailyAggregation(
                eq(LocalDate.of(2026, 6, 1)),
                eq(LocalDate.of(2026, 6, 30))))
                .thenReturn(List.of(row));

        mockMvc.perform(get("/api/diet-logs/daily-summary")
                        .param("from", "2026-06-01")
                        .param("to", "2026-06-30")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2026-06-10"))
                .andExpect(jsonPath("$[0].totalKcal").value(800))
                .andExpect(jsonPath("$[0].totalCarbsG").value(100.0))
                .andExpect(jsonPath("$[0].totalProteinG").value(50.0))
                .andExpect(jsonPath("$[0].totalFatG").value(20.0))
                .andExpect(jsonPath("$[0].count").value(3));
    }

    @Test
    void getDailySummary_missingToParam_returns5xx() throws Exception {
        // GlobalExceptionHandler가 MissingServletRequestParameterException을 처리하지 않아 500 반환
        mockMvc.perform(get("/api/diet-logs/daily-summary")
                        .param("from", "2026-06-01"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void getDailySummary_emptyResult_returnsEmptyArray() throws Exception {
        when(dietLogService.getDailyAggregation(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/diet-logs/daily-summary")
                        .param("from", "2026-06-01")
                        .param("to", "2026-06-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
