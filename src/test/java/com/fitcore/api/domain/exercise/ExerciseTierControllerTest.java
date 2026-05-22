package com.fitcore.api.domain.exercise;

import com.fitcore.api.domain.exercise.response.ExerciseTierResponse;
import com.fitcore.api.domain.exercise.service.ExerciseTierService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ExerciseTierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExerciseTierService exerciseTierService;

    private ExerciseTierResponse stubExercise(String nameEn, String primaryMuscle) {
        return ExerciseTierResponse.builder()
            .nameEn(nameEn)
            .nameKr(nameEn)
            .primaryMuscle(primaryMuscle)
            .equipment("BARBELL")
            .tier(1L)
            .secondaryMuscle(List.of())
            .build();
    }

    @Test
    void getExerciseCatalog_returnsList() throws Exception {
        when(exerciseTierService.getAllExerciseTiers()).thenReturn(List.of(
            stubExercise("Barbell Bench Press", "chest"),
            stubExercise("Barbell Squat", "quadriceps")
        ));

        mockMvc.perform(get("/api/exercises/catalog"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].nameEn").value("Barbell Bench Press"))
            .andExpect(jsonPath("$[0].primaryMuscle").value("chest"))
            .andExpect(jsonPath("$[1].primaryMuscle").value("quadriceps"));
    }

    @Test
    void getExerciseCatalog_emptyDb_returnsEmptyList() throws Exception {
        when(exerciseTierService.getAllExerciseTiers()).thenReturn(List.of());

        mockMvc.perform(get("/api/exercises/catalog"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getRecentRecord_noRecord_returns404() throws Exception {
        when(exerciseTierService.getRecentRecord("barbell_bench_press")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/exercises/barbell_bench_press/recent-record"))
            .andExpect(status().isNotFound());
    }
}
