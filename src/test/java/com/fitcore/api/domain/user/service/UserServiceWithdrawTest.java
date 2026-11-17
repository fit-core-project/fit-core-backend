package com.fitcore.api.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.fitcore.api.domain.user.entity.SocialAccountEntity;
import com.fitcore.api.domain.user.entity.UserProfileEntity;
import com.fitcore.api.domain.user.enums.UserStatus;
import com.fitcore.api.domain.user.repository.SocialAccountRepository;
import com.fitcore.api.domain.user.repository.UserRepository;
import com.fitcore.api.global.common.util.SecurityUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceWithdrawTest {

    @Mock
    private SecurityUtils securityUtils;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SocialAccountRepository socialAccountRepository;
    @InjectMocks
    private UserService userService;

    @Test
    void withdrawMe_unlinksSocialAccounts_anonymizes_thenSoftDeletes() {
        UserProfileEntity user = UserProfileEntity.builder()
            .email("bye@fitcore.com")
            .nickname("bye")
            .status(UserStatus.ACTIVE)
            .build();
        ReflectionTestUtils.setField(user, "userId", "u-1");
        List<SocialAccountEntity> socials = List.of(
            SocialAccountEntity.builder().user(user).provider("kakao").providerId("k-1").build());

        when(securityUtils.getCurrentUserId()).thenReturn("u-1");
        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(socialAccountRepository.findByUserUserId("u-1")).thenReturn(socials);

        userService.withdrawMe("10.0.0.1");

        InOrder order = inOrder(socialAccountRepository, userRepository);
        order.verify(socialAccountRepository).deleteAll(socials);
        order.verify(userRepository).saveAndFlush(user);
        order.verify(userRepository).delete(user);
        assertThat(user.getEmail()).isEqualTo("withdrawn+u-1@withdrawn.fitcore.invalid");
        assertThat(user.getNickname()).isNull();
        assertThat(user.getDeletedIp()).isEqualTo("10.0.0.1");
    }
}
