package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.INVALID_CREDENTIALS;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.USER_ALREADY_EXISTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssafy.enjoytrip.core.domain.Member;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.model.AuthLogRecord;
import com.ssafy.enjoytrip.storage.db.core.model.MemberRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AuthLogMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class MemberServiceTest {
    private MemberMapper memberMapper;
    private AuthLogMapper authLogMapper;
    private PasswordEncoder passwordEncoder;
    private MemberService service;

    @BeforeEach
    void setUp() {
        memberMapper = mock(MemberMapper.class);
        authLogMapper = mock(AuthLogMapper.class);
        passwordEncoder = new BCryptPasswordEncoder();
        service = new MemberService(passwordEncoder, memberMapper, authLogMapper);
    }

    @DisplayName("회원가입은 BCrypt 비밀번호를 db-core MemberRecord로 저장한다")
    @Test
    void signupStoresBcryptPassword() {
        when(memberMapper.existsByEmail("Case@Test.example")).thenReturn(0);

        service.signup(new Member(null, "SSAFY", "SSAFY", "Case@Test.example", "secret123", null));

        ArgumentCaptor<MemberRecord> memberCaptor = ArgumentCaptor.forClass(MemberRecord.class);
        verify(memberMapper).insert(memberCaptor.capture());
        MemberRecord saved = memberCaptor.getValue();
        assertThat(saved.getEmail()).isEqualTo("Case@Test.example");
        assertThat(saved.getPassword()).isNotEqualTo("secret123");
        assertThat(passwordEncoder.matches("secret123", saved.getPassword())).isTrue();
    }

    @DisplayName("회원가입은 이미 존재하는 이메일을 거부한다")
    @Test
    void signupRejectsExistingEmail() {
        when(memberMapper.existsByEmail("ssafy@example.com")).thenReturn(1);

        assertThatThrownBy(() -> service.signup(
                new Member(null, "SSAFY", "SSAFY", "ssafy@example.com", "secret123", null)
        ))
                .isInstanceOfSatisfying(CoreException.class,
                        exception -> assertThat(exception.errorType()).isEqualTo(USER_ALREADY_EXISTS));

        verify(memberMapper, never()).insert(any());
    }

    @DisplayName("로그인은 비밀번호가 일치하면 회원을 반환하고 회원 ID로 로그인 로그를 저장한다")
    @Test
    void loginReturnsMemberWhenPasswordMatches() {
        String encodedPassword = passwordEncoder.encode("secret");
        MemberRecord record = member(1L, "SSAFY", null, "Case@Test.example", encodedPassword);
        when(memberMapper.findByEmail("Case@Test.example")).thenReturn(record);

        Member loggedIn = service.login("Case@Test.example", "secret");

        assertThat(loggedIn.memberId()).isEqualTo(1L);
        ArgumentCaptor<AuthLogRecord> logCaptor = ArgumentCaptor.forClass(AuthLogRecord.class);
        verify(authLogMapper).insert(logCaptor.capture());
        assertThat(logCaptor.getValue().getMemberId()).isEqualTo(1L);
        verify(memberMapper, never()).update(any());
    }

    @DisplayName("로그인은 비밀번호가 일치하지 않으면 실패한다")
    @Test
    void loginFailsWhenPasswordDoesNotMatch() {
        MemberRecord record = member(
                1L,
                "SSAFY",
                null,
                "ssafy@example.com",
                passwordEncoder.encode("secret")
        );
        when(memberMapper.findByEmail("ssafy@example.com")).thenReturn(record);

        assertThatThrownBy(() -> service.login("ssafy@example.com", "wrong"))
                .isInstanceOfSatisfying(CoreException.class,
                        exception -> assertThat(exception.errorType()).isEqualTo(INVALID_CREDENTIALS));

        verify(authLogMapper, never()).insert(any());
    }

    @DisplayName("로그인은 평문으로 저장된 이전 비밀번호를 허용하거나 재암호화하지 않는다")
    @Test
    void loginRejectsPlainStoredPasswordWithoutUpgrade() {
        MemberRecord record = member(1L, "SSAFY", null, "ssafy@example.com", "secret");
        when(memberMapper.findByEmail("ssafy@example.com")).thenReturn(record);

        assertThatThrownBy(() -> service.login("ssafy@example.com", "secret"))
                .isInstanceOfSatisfying(CoreException.class,
                        exception -> assertThat(exception.errorType()).isEqualTo(INVALID_CREDENTIALS));

        verify(memberMapper, never()).update(any());
        verify(authLogMapper, never()).insert(any());
    }

    @DisplayName("OAuth 회원가입은 이름과 닉네임을 분리해 저장한다")
    @Test
    void oauthSignupStoresNicknameSeparatelyFromName() {
        when(memberMapper.existsByEmail("google@example.com")).thenReturn(0);

        Member member = service.signupWithOAuth("google", "123", "google@example.com", "김구글", "트래블러");

        ArgumentCaptor<MemberRecord> memberCaptor = ArgumentCaptor.forClass(MemberRecord.class);
        verify(memberMapper).insert(memberCaptor.capture());
        MemberRecord saved = memberCaptor.getValue();
        assertThat(member.name()).isEqualTo("김구글");
        assertThat(member.nickname()).isEqualTo("트래블러");
        assertThat(saved.getName()).isEqualTo("김구글");
        assertThat(saved.getNickname()).isEqualTo("트래블러");
    }

    @DisplayName("OAuth 회원가입은 이미 존재하는 이메일이면 충돌로 거부한다")
    @Test
    void oauthSignupRejectsExistingEmail() {
        when(memberMapper.existsByEmail("google@example.com")).thenReturn(1);

        assertThatThrownBy(() -> service.signupWithOAuth(
                "google",
                "123",
                "google@example.com",
                "김구글",
                "트래블러"
        ))
                .isInstanceOfSatisfying(CoreException.class,
                        exception -> assertThat(exception.errorType()).isEqualTo(USER_ALREADY_EXISTS));

        verify(memberMapper, never()).insert(any());
        verify(authLogMapper, never()).insert(any());
    }

    private static MemberRecord member(Long id, String name, String nickname, String email, String password) {
        MemberRecord member = new MemberRecord(name, nickname, email, password, null);
        member.setId(id);
        return member;
    }
}
