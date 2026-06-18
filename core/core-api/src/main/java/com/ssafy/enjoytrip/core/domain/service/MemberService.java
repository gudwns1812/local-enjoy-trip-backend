package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.EMAIL_ALREADY_EXISTS;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.INVALID_CREDENTIALS;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.USER_ALREADY_EXISTS;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.USER_NOT_FOUND;

import com.ssafy.enjoytrip.core.domain.Member;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.entity.AuthLogEntity;
import com.ssafy.enjoytrip.storage.db.core.entity.MemberEntity;
import com.ssafy.enjoytrip.storage.db.core.jpa.AuthLogJpaRepository;
import com.ssafy.enjoytrip.storage.db.core.jpa.MemberJpaRepository;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final PasswordEncoder passwordEncoder;

    public List<Member> findAllUsers() {
        return memberRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(entity -> new Member(
                        entity.getUserId(),
                        entity.getName(),
                        entity.getNickname(),
                        entity.getEmail(),
                        entity.getPassword(),
                        entity.getProfileImageUrl(),
                        entity.getRepresentativeLatitude(),
                        entity.getRepresentativeLongitude(),
                        entity.getRepresentativeRegionName(),
                        stringValue(entity.getCreatedAt())
                ))
                .toList();
    }

    public Member findByUserId(String userId) {
        return memberRepository.findByUserId(userId)
                .map(entity -> new Member(
                        entity.getUserId(),
                        entity.getName(),
                        entity.getNickname(),
                        entity.getEmail(),
                        entity.getPassword(),
                        entity.getProfileImageUrl(),
                        entity.getRepresentativeLatitude(),
                        entity.getRepresentativeLongitude(),
                        entity.getRepresentativeRegionName(),
                        stringValue(entity.getCreatedAt())
                ))
                .orElse(null);
    }

    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .map(entity -> new Member(
                        entity.getUserId(),
                        entity.getName(),
                        entity.getNickname(),
                        entity.getEmail(),
                        entity.getPassword(),
                        entity.getProfileImageUrl(),
                        entity.getRepresentativeLatitude(),
                        entity.getRepresentativeLongitude(),
                        entity.getRepresentativeRegionName(),
                        stringValue(entity.getCreatedAt())
                ))
                .orElse(null);
    }

    public Member findRequiredByUserId(String userId) {
        return memberRepository.findByUserId(userId)
                .map(entity -> new Member(
                        entity.getUserId(),
                        entity.getName(),
                        entity.getNickname(),
                        entity.getEmail(),
                        entity.getPassword(),
                        entity.getProfileImageUrl(),
                        entity.getRepresentativeLatitude(),
                        entity.getRepresentativeLongitude(),
                        entity.getRepresentativeRegionName(),
                        stringValue(entity.getCreatedAt())
                ))
                .orElseThrow(() -> new CoreException(USER_NOT_FOUND));
    }

    @Transactional
    public void signup(Member member) {
        validateNewMember(member);
        saveMember(member.withPassword(passwordEncoder.encode(member.password())));
    }

    @Transactional
    public Member login(String userId, String password) {
        Member member = findAuthenticatableMember(userId, password);
        upgradeLegacyPasswordIfNeeded(member, password);
        authLogRepository.save(new AuthLogEntity(member.userId(), "LOGIN"));
        return member;
    }

    @Transactional
    public Member loginWithOAuth(String provider, String providerUserId, String email, String name) {
        Member existing = memberRepository.findByEmail(email).map(entity -> new Member(
                        entity.getUserId(),
                        entity.getName(),
                        entity.getNickname(),
                        entity.getEmail(),
                        entity.getPassword(),
                        entity.getProfileImageUrl(),
                        entity.getRepresentativeLatitude(),
                        entity.getRepresentativeLongitude(),
                        entity.getRepresentativeRegionName(),
                        stringValue(entity.getCreatedAt())
                ))
                .orElse(null);
        if (existing != null) {
            authLogRepository.save(new AuthLogEntity(existing.userId(), "LOGIN"));
            return existing;
        }

        Member member = createOAuthMember(provider, providerUserId, email, name);
        saveMember(member);
        authLogRepository.save(new AuthLogEntity(member.userId(), "LOGIN"));
        return member;
    }

    @Transactional
    public Member signupWithOAuth(String provider, String providerUserId, String email, String name) {
        Member existing = memberRepository.findByEmail(email).map(entity -> new Member(
                        entity.getUserId(),
                        entity.getName(),
                        entity.getNickname(),
                        entity.getEmail(),
                        entity.getPassword(),
                        entity.getProfileImageUrl(),
                        entity.getRepresentativeLatitude(),
                        entity.getRepresentativeLongitude(),
                        entity.getRepresentativeRegionName(),
                        stringValue(entity.getCreatedAt())
                ))
                .orElse(null);
        if (existing != null) {
            authLogRepository.save(new AuthLogEntity(existing.userId(), "LOGIN"));
            return existing;
        }

        Member member = createOAuthMember(provider, providerUserId, email, name);
        saveMember(member);
        authLogRepository.save(new AuthLogEntity(member.userId(), "LOGIN"));
        return member;
    }

    public void logout(String userId) {
        authLogRepository.save(new AuthLogEntity(userId, "LOGOUT"));
    }

    public String findPassword(String userId, String email) {
        return memberRepository.findByUserIdAndEmail(userId, email)
                .map(MemberEntity::getPassword)
                .orElse(null);
    }

    @Transactional
    public void update(Member member) {
        validateEmailOwner(member);
        if (!updateMemberEntity(member.withPassword(encodedPasswordWhenPresent(member.password())))) {
            throw new CoreException(USER_NOT_FOUND);
        }
    }

    @Transactional
    public void delete(String userId) {
        if (!memberRepository.existsByUserId(userId)) {
            throw new CoreException(USER_NOT_FOUND);
        }
        memberRepository.deleteByUserId(userId);
    }

    private void validateNewMember(Member member) {
        if (memberRepository.existsByUserId(member.userId())) {
            throw new CoreException(USER_ALREADY_EXISTS);
        }
        if (memberRepository.existsByEmail(member.email())) {
            throw new CoreException(EMAIL_ALREADY_EXISTS);
        }
    }

    private void validateEmailOwner(Member member) {
        if (member.email() == null) {
            return;
        }
        Member owner = memberRepository.findByEmail(member.email()).map(entity -> new Member(
                        entity.getUserId(),
                        entity.getName(),
                        entity.getNickname(),
                        entity.getEmail(),
                        entity.getPassword(),
                        entity.getProfileImageUrl(),
                        entity.getRepresentativeLatitude(),
                        entity.getRepresentativeLongitude(),
                        entity.getRepresentativeRegionName(),
                        stringValue(entity.getCreatedAt())
                ))
                .orElse(null);
        if (owner != null && !owner.userId().equals(member.userId())) {
            throw new CoreException(EMAIL_ALREADY_EXISTS);
        }
    }

    private Member findAuthenticatableMember(String userId, String password) {
        Member member = memberRepository.findByUserId(userId).map(entity -> new Member(
                        entity.getUserId(),
                        entity.getName(),
                        entity.getNickname(),
                        entity.getEmail(),
                        entity.getPassword(),
                        entity.getProfileImageUrl(),
                        entity.getRepresentativeLatitude(),
                        entity.getRepresentativeLongitude(),
                        entity.getRepresentativeRegionName(),
                        stringValue(entity.getCreatedAt())
                ))
                .orElse(null);
        if (member == null || !matchesPassword(password, member.password())) {
            throw new CoreException(INVALID_CREDENTIALS);
        }
        return member;
    }

    private void upgradeLegacyPasswordIfNeeded(Member member, String password) {
        if (shouldUpgradePassword(member.password())) {
            updateMemberEntity(member.withPassword(passwordEncoder.encode(password)));
        }
    }

    private String encodedPasswordWhenPresent(String password) {
        if (isBlank(password)) {
            return password;
        }
        return passwordEncoder.encode(password);
    }

    private boolean matchesPassword(String rawPassword, String storedPassword) {
        if (isBlank(rawPassword) || isBlank(storedPassword)) {
            return false;
        }
        if (isEncodedPassword(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return storedPassword.equals(rawPassword);
    }

    private boolean shouldUpgradePassword(String password) {
        return !isBlank(password) && !isEncodedPassword(password);
    }

    private boolean isEncodedPassword(String password) {
        return password != null && password.startsWith("$2");
    }

    private Member createOAuthMember(String provider, String providerUserId, String email, String name) {
        return new Member(
                oauthUserId(provider, providerUserId),
                name,
                email,
                passwordEncoder.encode(UUID.randomUUID().toString()),
                ""
        );
    }

    private String oauthUserId(String provider, String providerUserId) {
        String normalizedProvider = provider.toLowerCase(Locale.ROOT);
        String sourceId = providerUserId;
        String normalizedId = sourceId.replaceAll("[^A-Za-z0-9_]", "");
        if (normalizedId.isEmpty()) {
            normalizedId = Integer.toUnsignedString(sourceId.hashCode(), 36);
        }
        String userId = normalizedProvider + "_" + normalizedId;
        if (userId.length() <= 64) {
            return uniqueOauthUserId(userId, normalizedProvider);
        }
        return uniqueOauthUserId(
                normalizedProvider + "_" + Integer.toUnsignedString(sourceId.hashCode(), 36),
                normalizedProvider
        );
    }

    private String uniqueOauthUserId(String candidate, String provider) {
        if (!memberRepository.existsByUserId(candidate)) {
            return candidate;
        }
        return provider + "_" + UUID.randomUUID().toString().replace("-", "");
    }

    private final MemberJpaRepository memberRepository;
    private final AuthLogJpaRepository authLogRepository;

    private void saveMember(Member member) {
        memberRepository.save(new MemberEntity(
                member.userId(),
                member.name(),
                member.nickname(),
                member.email(),
                member.password(),
                member.profileImageUrl(),
                member.representativeLatitude(),
                member.representativeLongitude(),
                member.representativeRegionName()
        ));
    }

    private boolean updateMemberEntity(Member member) {
        return memberRepository.findByUserId(member.userId())
                .map(entity -> {
                    entity.update(
                            member.name(),
                            member.nickname(),
                            member.email(),
                            member.password(),
                            member.profileImageUrl(),
                            member.representativeLatitude(),
                            member.representativeLongitude(),
                            member.representativeRegionName()
                    );
                    return true;
                })
                .orElse(false);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String stringValue(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }
}
