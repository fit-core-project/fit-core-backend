package com.fitcore.api.domain.workout;

import com.fitcore.api.domain.workout.response.AttendanceWeekResponse;
import com.fitcore.api.domain.workout.response.PrResponse;
import com.fitcore.api.domain.workout.service.WorkoutSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class WorkoutSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkoutSessionService workoutSessionService;

    private PrResponse stubPr(String exerciseId, String name, double orm, double weight, int reps) {
        return PrResponse.builder()
            .exerciseId(exerciseId)
            .exerciseNameSnapshot(name)
            .estimated1RM(BigDecimal.valueOf(orm).setScale(1, java.math.RoundingMode.HALF_UP))
            .weightKg(BigDecimal.valueOf(weight))
            .reps(reps)
            .achievedDate(LocalDate.of(2026, 5, 15))
            .build();
    }

    private AttendanceWeekResponse stubWeek(LocalDate weekStart, int actual, Integer target, Double rate) {
        return AttendanceWeekResponse.builder()
            .weekStart(weekStart)
            .weekEnd(weekStart.plusDays(6))
            .actualDays(actual)
            .targetDays(target)
            .rate(rate)
            .build();
    }

    // ── PR 엔드포인트 ──────────────────────────────────────────────

    @Test
    void getPrs_returnsSortedList() throws Exception {
        when(workoutSessionService.getPrs()).thenReturn(List.of(
            stubPr("barbell_squat", "Barbell Squat", 142.5, 120.0, 5),
            stubPr("barbell_bench_press", "Barbell Bench Press", 110.0, 100.0, 3)
        ));

        mockMvc.perform(get("/api/workouts/prs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].exerciseId").value("barbell_squat"))
            .andExpect(jsonPath("$[0].estimated1RM").value(142.5))
            .andExpect(jsonPath("$[0].weightKg").value(120.0))
            .andExpect(jsonPath("$[0].reps").value(5))
            .andExpect(jsonPath("$[1].exerciseId").value("barbell_bench_press"));
    }

    @Test
    void getPrs_noWorkouts_returnsEmptyList() throws Exception {
        when(workoutSessionService.getPrs()).thenReturn(List.of());

        mockMvc.perform(get("/api/workouts/prs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    // ── 출석률 엔드포인트 ──────────────────────────────────────────

    @Test
    void getAttendance_withTargetDays_returnsRates() throws Exception {
        LocalDate monday = LocalDate.of(2026, 5, 4);
        when(workoutSessionService.getAttendance()).thenReturn(List.of(
            stubWeek(monday,              3, 4, 0.75),
            stubWeek(monday.plusWeeks(1), 4, 4, 1.0),
            stubWeek(monday.plusWeeks(2), 2, 4, 0.5),
            stubWeek(monday.plusWeeks(3), 1, 4, 0.25)
        ));

        mockMvc.perform(get("/api/workouts/attendance"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(4))
            .andExpect(jsonPath("$[0].actualDays").value(3))
            .andExpect(jsonPath("$[0].targetDays").value(4))
            .andExpect(jsonPath("$[0].rate").value(0.75))
            .andExpect(jsonPath("$[1].rate").value(1.0))
            .andExpect(jsonPath("$[0].weekStart").value("2026-05-04"))
            .andExpect(jsonPath("$[0].weekEnd").value("2026-05-10"));
    }

    @Test
    void getAttendance_noTargetDays_rateIsNull() throws Exception {
        LocalDate monday = LocalDate.of(2026, 5, 18);
        when(workoutSessionService.getAttendance()).thenReturn(List.of(
            stubWeek(monday, 2, null, null)
        ));

        mockMvc.perform(get("/api/workouts/attendance"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].actualDays").value(2))
            .andExpect(jsonPath("$[0].targetDays").isEmpty())
            .andExpect(jsonPath("$[0].rate").isEmpty());
    }

    @Test
    void getAttendance_noWorkouts_returnsZeroActualDays() throws Exception {
        LocalDate monday = LocalDate.of(2026, 5, 18);
        when(workoutSessionService.getAttendance()).thenReturn(List.of(
            stubWeek(monday, 0, 4, 0.0)
        ));

        mockMvc.perform(get("/api/workouts/attendance"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].actualDays").value(0))
            .andExpect(jsonPath("$[0].rate").value(0.0));
    }
}
