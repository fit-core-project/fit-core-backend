package com.fitcore.api.domain.nutrition;

import com.fitcore.api.domain.nutrition.entity.DietLogEntity;
import com.fitcore.api.domain.nutrition.repository.DietLogRepository;
import com.fitcore.api.domain.nutrition.request.DietLogRequest;
import com.fitcore.api.domain.nutrition.response.DietLogResponse;
import com.fitcore.api.domain.nutrition.response.DietSummaryResponse;
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

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DietLogMicronutrientTest {

    @Mock DietLogRepository dietLogRepository;
    @Mock SecurityUtils securityUtils;
    @InjectMocks DietLogService dietLogService;

    private static final LocalDate DATE = LocalDate.of(2026, 6, 20);

    @BeforeEach
    void setup() {
        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
        when(dietLogRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    // ── 저장 검증 ──────────────────────────────────────────────────────────────

    @Test
    void db_item_sugarFiberSodium_savedAsGiven() {
        DietLogRequest req = req("db", 212, bd(46.0), bd(0.0), bd(2.0),
                bd(0.6), bd(1.8), 3);

        List<DietLogResponse> result = dietLogService.saveBatch(List.of(req));

        DietLogResponse r = result.get(0);
        assertThat(r.getSugarG()).isEqualByComparingTo("0.6");
        assertThat(r.getFiberG()).isEqualByComparingTo("1.8");
        assertThat(r.getSodiumMg()).isEqualTo(3);
    }

    @Test
    void ai_item_sugarFiberSodium_savedAsNull() {
        // AI 항목은 sugar/fiber/sodium 없음 → null 저장, 0 위조 금지
        DietLogRequest req = req("ai", null, bd(46.0), bd(0.0), bd(2.0),
                null, null, null);

        List<DietLogResponse> result = dietLogService.saveBatch(List.of(req));

        DietLogResponse r = result.get(0);
        assertThat(r.getSugarG()).isNull();
        assertThat(r.getFiberG()).isNull();
        assertThat(r.getSodiumMg()).isNull();
    }

    // ── 일별 합계 검증 ─────────────────────────────────────────────────────────

    @Test
    void summary_nonNullSum_sugarFiberSodium() {
        // db 항목: sugar=1.2, fiber=3.0, sodium=300
        // ai 항목: sugar=null, fiber=null, sodium=null
        // 합계: sugar=1.2, fiber=3.0, sodium=300 (null 항목 스킵)
        DietLogEntity dbItem = entity(212, 46.0, 0.0, 2.0, 1.2, 3.0, 300, "db");
        DietLogEntity aiItem = entity(200, 30.0, 20.0, 8.0, null, null, null, "ai");

        when(dietLogRepository.findByUserIdAndLogDate("user-1", DATE))
                .thenReturn(List.of(dbItem, aiItem));

        DietSummaryResponse summary = dietLogService.getSummary(DATE);

        assertThat(summary.getTotalSugarG()).isEqualByComparingTo("1.2");
        assertThat(summary.getTotalFiberG()).isEqualByComparingTo("3.0");
        assertThat(summary.getTotalSodiumMg()).isEqualTo(300);
    }

    @Test
    void summary_allNull_sugarFiberSodium_returnsZero() {
        // 모든 항목 sugar/fiber/sodium null → 합계 0
        DietLogEntity aiItem1 = entity(200, 30.0, 20.0, 8.0, null, null, null, "ai");
        DietLogEntity aiItem2 = entity(100, 10.0, 15.0, 3.0, null, null, null, "ai");

        when(dietLogRepository.findByUserIdAndLogDate("user-1", DATE))
                .thenReturn(List.of(aiItem1, aiItem2));

        DietSummaryResponse summary = dietLogService.getSummary(DATE);

        assertThat(summary.getTotalSugarG()).isEqualByComparingTo("0.0");
        assertThat(summary.getTotalFiberG()).isEqualByComparingTo("0.0");
        assertThat(summary.getTotalSodiumMg()).isEqualTo(0);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static DietLogRequest req(String source, Integer kcal,
                                      BigDecimal p, BigDecimal c, BigDecimal f,
                                      BigDecimal sugar, BigDecimal fiber, Integer sodium) {
        DietLogRequest r = new DietLogRequest();
        set(r, "logDate", DATE);
        set(r, "foodName", "테스트식품");
        set(r, "source", source);
        set(r, "kcal", kcal);
        set(r, "proteinG", p);
        set(r, "carbsG", c);
        set(r, "fatG", f);
        set(r, "sugarG", sugar);
        set(r, "fiberG", fiber);
        set(r, "sodiumMg", sodium);
        return r;
    }

    private static DietLogEntity entity(int kcal,
                                        double protein, double carbs, double fat,
                                        Double sugar, Double fiber, Integer sodium,
                                        String source) {
        return DietLogEntity.builder()
                .userId("user-1")
                .logDate(DATE)
                .foodName("테스트식품")
                .kcal(kcal)
                .proteinG(bd(protein))
                .carbsG(bd(carbs))
                .fatG(bd(fat))
                .sugarG(sugar != null ? bd(sugar) : null)
                .fiberG(fiber != null ? bd(fiber) : null)
                .sodiumMg(sodium)
                .source(source)
                .build();
    }

    private static BigDecimal bd(double v) { return BigDecimal.valueOf(v); }

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
