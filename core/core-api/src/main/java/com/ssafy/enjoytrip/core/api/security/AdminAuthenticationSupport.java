package com.ssafy.enjoytrip.core.api.security;

import com.ssafy.enjoytrip.storage.db.core.model.MemberRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminAuthenticationSupport {
    private final MemberMapper memberMapper;

    public boolean isAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        MemberRecord member = findAuthenticatedMember(authentication);
        //TODO: ADMIN을 문자열로 쓰는것보다 enum 상수 ADMIN으로 쓰는게 나을듯
        return member != null && "ADMIN".equals(member.getRole());
    }

    public String requireAdminUserId(Authentication authentication) {
        MemberRecord member = findAuthenticatedMember(authentication);
        if (member == null || !"ADMIN".equals(member.getRole())) {
            //TODO: 인가 문제는 여기서 하기 보다 spring security에서 처리하는게 좋을듯.
            throw new AccessDeniedException("관리자 권한이 필요합니다.");
        }
        return member.getUserId();
    }

    private MemberRecord findAuthenticatedMember(Authentication authentication) {
        return memberMapper.findByUserId(authentication.getName());
    }
}
