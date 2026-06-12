package com.fitcore.api.domain.user;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.fitcore.api.domain.user.entity.UserProfileEntity;
import com.fitcore.api.domain.user.enums.GoalType;
import com.fitcore.api.domain.user.enums.UserStatus;
import com.fitcore.api.domain.user.response.UserPreferencesResponse;
import com.fitcore.api.domain.user.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UserPreferencesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void getUserPreferences_nullProfileFields_returnsDefaults() throws Exception {
        UserProfileEntity user = UserProfileEntity.builder()
            .email("new-user@fitcore.com")
            .name("New User")
            .status(UserStatus.ACTIVE)
            .build();
        ReflectionTestUtils.setField(user, "timeAvailable", null);
        ReflectionTestUtils.setField(user, "goalType", null);
        ReflectionTestUtils.setField(user, "equipmentAccess", null);
        ReflectionTestUtils.setField(user, "trainingDaysPerWeek", null);

        when(userService.getUserPreferences()).thenReturn(new UserPreferencesResponse(user));

        mockMvc.perform(get("/api/users/preferences"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.timeAvailable").value(60))
            .andExpect(jsonPath("$.goal").value(GoalType.generalFitness.name()))
            .andExpect(jsonPath("$.equipment").isArray())
            .andExpect(jsonPath("$.equipment").isEmpty())
            .andExpect(jsonPath("$.weeklyFrequency").value(3));
    }
}
