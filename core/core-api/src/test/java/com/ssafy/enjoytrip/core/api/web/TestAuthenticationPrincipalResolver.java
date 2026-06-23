package com.ssafy.enjoytrip.core.api.web;

import com.ssafy.enjoytrip.core.api.security.AuthenticatedMemberId;
import java.security.Principal;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
class TestAuthenticationPrincipalResolver implements HandlerMethodArgumentResolver {
    private static final String AUTHENTICATION_REQUIRED_MESSAGE = "인증이 필요합니다.";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthenticatedMemberId.class)
                && Long.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        AuthenticatedMemberId annotation = parameter.getParameterAnnotation(AuthenticatedMemberId.class);
        Principal principal = webRequest.getUserPrincipal();
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            return unauthenticatedValue(annotation.unauthenticated());
        }
        return Long.valueOf(principal.getName());
    }

    private static Long unauthenticatedValue(AuthenticatedMemberId.Unauthenticated policy) {
        return switch (policy) {
            case THROW -> throw new AuthenticationCredentialsNotFoundException(AUTHENTICATION_REQUIRED_MESSAGE);
            case NULL -> null;
        };
    }
}
