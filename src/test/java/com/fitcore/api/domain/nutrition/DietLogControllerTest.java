package com.fitcore.api.domain.nutrition;

import com.fitcore.api.domain.nutrition.response.DietLogResponse;
import com.fitcore.api.domain.nutrition.service.DietLogService;
import com.fitcore.api.global.error.ErrorCode;
import com.fitcore.api.global.error.exception.BusinessException;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class DietLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DietLogService dietLogService;

    private DietLogResponse stubResponse(String id, String foodName, int kcal) {
        return DietLogResponse.builder()
                .id(id)
                .logDate(LocalDate.of(2026, 6, 16))
                .mealType("breakfast")
                .foodName(foodName)
                .kcal(kcal)
                .proteinG(BigDecimal.valueOf(20.0))
                .carbsG(BigDecimal.valueOf(30.0))
                .fatG(BigDecimal.valueOf(10.0))
                .source("manual")
                .build();
    }

    // ── DELETE ────────────────────────────────────────────────────

    @Test
    void delete_success_returns204() throws Exception {
        doNothing().when(dietLogService).delete("abc123");

        mockMvc.perform(delete("/api/diet-logs/abc123"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        doThrow(new BusinessException(ErrorCode.NOT_FOUND))
                .when(dietLogService).delete("missing");

        mockMvc.perform(delete("/api/diet-logs/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_wrongOwner_returns404() throws Exception {
        doThrow(new BusinessException(ErrorCode.NOT_FOUND))
                .when(dietLogService).delete("other-user-id");

        mockMvc.perform(delete("/api/diet-logs/other-user-id"))
                .andExpect(status().isNotFound());
    }

    // ── PUT ───────────────────────────────────────────────────────

    @Test
    void update_withKcal_returns200() throws Exception {
        when(dietLogService.update(eq("abc123"), any()))
                .thenReturn(stubResponse("abc123", "닭가슴살", 300));

        mockMvc.perform(put("/api/diet-logs/abc123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "foodName": "닭가슴살",
                                  "mealType": "lunch",
                                  "kcal": 300,
                                  "proteinG": 30.0,
                                  "carbsG": 10.0,
                                  "fatG": 5.0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("abc123"))
                .andExpect(jsonPath("$.foodName").value("닭가슴살"))
                .andExpect(jsonPath("$.kcal").value(300))
                .andExpect(jsonPath("$.source").value("manual"));
    }

    @Test
    void update_macrosOnly_kcalCalculatedBy449() throws Exception {
        // protein=20, carbs=30, fat=10 → 20*4 + 30*4 + 10*9 = 80+120+90 = 290
        when(dietLogService.update(eq("abc123"), any()))
                .thenReturn(stubResponse("abc123", "현미밥", 290));

        mockMvc.perform(put("/api/diet-logs/abc123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "foodName": "현미밥",
                                  "mealType": "breakfast",
                                  "proteinG": 20.0,
                                  "carbsG": 30.0,
                                  "fatG": 10.0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kcal").value(290));
    }

    @Test
    void update_notFound_returns404() throws Exception {
        doThrow(new BusinessException(ErrorCode.NOT_FOUND))
                .when(dietLogService).update(eq("missing"), any());

        mockMvc.perform(put("/api/diet-logs/missing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "foodName": "사과",
                                  "kcal": 100
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_noFoodName_returns400() throws Exception {
        mockMvc.perform(put("/api/diet-logs/abc123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "kcal": 200
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_noKcalNoMacros_returns400() throws Exception {
        doThrow(new BusinessException(ErrorCode.INVALID_INPUT_VALUE))
                .when(dietLogService).update(eq("abc123"), any());

        mockMvc.perform(put("/api/diet-logs/abc123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "foodName": "모름"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
