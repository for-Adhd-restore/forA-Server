package com.project.foradhd.domain.auth.handler;

import com.project.foradhd.domain.auth.business.service.JwtService;
import com.project.foradhd.domain.auth.business.userdetails.impl.OAuth2UserImpl;
import com.project.foradhd.domain.user.business.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2UserImpl oAuth2User = (OAuth2UserImpl) authentication.getPrincipal();
        String accessToken = jwtService.generateAccessToken(oAuth2User.getUserId(), oAuth2User.getEmail(),
                oAuth2User.getAuthorities());
        String refreshToken = jwtService.generateRefreshToken(oAuth2User.getUserId());
        String userId = oAuth2User.getUserId();
        jwtService.saveRefreshToken(userId, refreshToken);

        Boolean hasVerifiedEmail = userService.hasVerifiedEmail(userId);
        Boolean hasProfile = userService.hasUserProfile(userId);

        String redirectUri = String.format(
                "kakao4c16ebfb9bd4bb5420dbca88502fe1ed://login-result" +
                        "?accessToken=%s&refreshToken=%s&hasVerifiedEmail=%s&hasProfile=%s",
                accessToken,
                refreshToken,
                hasVerifiedEmail,
                hasProfile
        );

        response.sendRedirect(redirectUri);
    }
}
