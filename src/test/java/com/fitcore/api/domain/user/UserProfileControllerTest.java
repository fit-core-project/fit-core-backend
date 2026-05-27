package com.fitcore.api.domain.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitcore.api.domain.user.response.UserProfileResponse;
import com.fitcore.api.domain.user.service.UserService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private UserProfileResponse stubProfile() {
        UserProfileResponse r = new UserProfileResponse();
        r.setUserId("test-user-001");
        r.setEmail("test@fitcore.com");
        r.setNickname("테스터");
        return r;
    }

    @Test
    void getMyProfile_returnsUserIdAndEmail() throws Exception {
        when(userService.getMyProfile()).thenReturn(stubProfile());

        mockMvc.perform(get("/api/profile/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value("test-user-001"))
            .andExpect(jsonPath("$.email").value("test@fitcore.com"))
            .andExpect(jsonPath("$.nickname").value("테스터"));
    }

    @Test
    void updateMyProfile_returnsUpdatedProfile() throws Exception {
        UserProfileResponse updated = stubProfile();
        updated.setNickname("새닉네임");
        when(userService.updateMyProfile(any())).thenReturn(updated);

        mockMvc.perform(put("/api/profile/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "nickname": "새닉네임",
                      "notes": "업데이트 테스트"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nickname").value("새닉네임"));
    }

    @Test
    void updateMyProfile_invalidNumericFields_returns400WithMessages() throws Exception {
        mockMvc.perform(put("/api/profile/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "bodyWeightKg": 19,
                      "bodyFatPct": 61,
                      "strengthBaseline": [
                        {
                          "exerciseId": "30",
                          "exerciseNameSnapshot": "Barbell Bench Press",
                          "workingWeightKg": 501,
                          "reps": 0
                        }
                      ],
                      "bodyCompositionSnapshot": [
                        {
                          "measuredAt": "2026-05-26",
                          "bodyWeightKg": 301,
                          "skeletalMuscleMassKg": 4,
                          "bodyFatPct": 0
                        }
                      ]
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("bodyWeightKg must be greater than or equal to 20 kg")))
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("bodyWeightKg must be less than or equal to 300 kg")))
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("bodyFatPct must be less than or equal to 60%")))
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("workingWeightKg must be less than or equal to 500 kg")))
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("reps must be greater than or equal to 1")))
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("skeletalMuscleMassKg must be greater than or equal to 5 kg")))
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("bodyFatPct must be greater than or equal to 1%")));
    }

    @Test
    void checkNicknameDuplicate_existingNickname_returnsTrue() throws Exception {
        when(userService.checkNicknameDuplicate("테스터")).thenReturn(true);

        mockMvc.perform(get("/api/profile/check-nickname/테스터"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(true));
    }

    @Test
    void checkNicknameDuplicate_availableNickname_returnsFalse() throws Exception {
        when(userService.checkNicknameDuplicate("새닉네임")).thenReturn(false);

        mockMvc.perform(get("/api/profile/check-nickname/새닉네임"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(false));
    }

    @Test
    void getMyProfile_unknownUser_returns404() throws Exception {
        when(userService.getMyProfile()).thenThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

        mockMvc.perform(get("/api/profile/me"))
            .andExpect(status().isNotFound());
    }
}
