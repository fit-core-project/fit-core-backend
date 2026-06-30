package com.fitcore.api.domain.nutrition;

import com.fitcore.api.domain.nutrition.entity.DietLogEntity;
import com.fitcore.api.domain.nutrition.repository.DietLogRepository;
import com.fitcore.api.domain.nutrition.request.DietLogRequest;
import com.fitcore.api.domain.nutrition.response.DietLogResponse;
import com.fitcore.api.domain.nutrition.service.DietLogService;
import com.fitcore.api.global.common.util.SecurityUtils;
import com.fitcore.api.global.error.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DietLogServiceKcalTest {

    @Mock DietLogRepository dietLogRepository;
    @Mock SecurityUtils securityUtils;
    @InjectMocks DietLogService dietLogService;

    @BeforeEach
    void setup() {
        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
        when(dietLogRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void db_withPositiveKcal_usesDbKcal() {
        // 닭가슴살 200g: DB kcal=212, 4·4·9 would give 46*4+0*4+2*9=202
        DietLogRequest req = req("db", 212,
                bd(46.0), bd(0.0), bd(2.0));

        List<DietLogResponse> result = dietLogService.saveBatch(List.of(req));
        assertThat(result.get(0).getKcal()).isEqualTo(212);
    }

    @Test
    void ai_ignoresKcalField_uses449() {
        // source=ai: kcal provided in request is ignored → 4·4·9 = 46*4+0*4+2*9 = 202
        DietLogRequest req = req("ai", 999,
                bd(46.0), bd(0.0), bd(2.0));

        List<DietLogResponse> result = dietLogService.saveBatch(List.of(req));
        assertThat(result.get(0).getKcal()).isEqualTo(202);
    }

    @Test
    void manual_withKcal_usesProvidedKcal() {
        DietLogRequest req = req("manual", 300,
                bd(20.0), bd(30.0), bd(10.0));

        List<DietLogResponse> result = dietLogService.saveBatch(List.of(req));
        assertThat(result.get(0).getKcal()).isEqualTo(300);
    }

    @Test
    void manual_withoutKcal_calculates449() {
        // protein=20, carbs=30, fat=10 → 20*4+30*4+10*9 = 290
        DietLogRequest req = req("manual", null,
                bd(20.0), bd(30.0), bd(10.0));

        List<DietLogResponse> result = dietLogService.saveBatch(List.of(req));
        assertThat(result.get(0).getKcal()).isEqualTo(290);
    }

    @Test
    void db_withoutKcal_fallsBackTo449() {
        // source=db but no kcal → fallback to macros
        DietLogRequest req = req("db", null,
                bd(46.0), bd(0.0), bd(2.0));

        List<DietLogResponse> result = dietLogService.saveBatch(List.of(req));
        assertThat(result.get(0).getKcal()).isEqualTo(202);
    }

    @Test
    void ai_withoutMacros_throws() {
        DietLogRequest req = req("ai", null, null, null, null);
        assertThatThrownBy(() -> dietLogService.saveBatch(List.of(req)))
                .isInstanceOf(BusinessException.class);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static DietLogRequest req(String source, Integer kcal,
                                      BigDecimal p, BigDecimal c, BigDecimal f) {
        DietLogRequest r = new DietLogRequest();
        set(r, "logDate", LocalDate.of(2026, 6, 17));
        set(r, "foodName", "테스트식품");
        set(r, "source", source);
        set(r, "kcal", kcal);
        set(r, "proteinG", p);
        set(r, "carbsG", c);
        set(r, "fatG", f);
        return r;
    }

    private static BigDecimal bd(double v) {
        return BigDecimal.valueOf(v);
    }

    private static void set(Object obj, String name, Object value) {
        try {
            Field f = obj.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
