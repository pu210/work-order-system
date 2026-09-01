package com.eeit219.work_order_system.common.security;

import java.io.IOException;

import org.json.JSONObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.eeit219.work_order_system.modules.a.dto.CurrentUserDTO;
import com.eeit219.work_order_system.modules.a.dto.LoginResponseDTO;
import com.eeit219.work_order_system.modules.a.service.OAuthLoginService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;

@Component
public class OAuth2LoginSuccessHandler
                implements AuthenticationSuccessHandler {

        public static final String SESSION_ATTRIBUTE = "OAUTH_LOGIN_RESULT";
        private final String frontendUrl;

        private final OAuthLoginService oauthLoginService;
        private final JsonWebTokenUtility jwtUtility;

        public OAuth2LoginSuccessHandler(
                        OAuthLoginService oauthLoginService,
                        JsonWebTokenUtility jwtUtility,
                        @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl) {
                this.oauthLoginService = oauthLoginService;
                this.jwtUtility = jwtUtility;
                this.frontendUrl = frontendUrl.replaceAll("/+$", "");
        }

        @Override
        public void onAuthenticationSuccess(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        Authentication authentication)
                        throws IOException, ServletException {

                try {
                        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

                        CurrentUserDTO user = oauthLoginService.loginWithGoogle(oauth2User);

                        JSONObject tokenData = new JSONObject()
                                        .put("userId", user.userId())
                                        .put("account", user.account())
                                        .put("name", user.name())
                                        .put(
                                                        "mustChangePassword",
                                                        user.mustChangePassword())
                                        .put("roleCodes", user.roleCodes());

                        String token = jwtUtility.createToken(tokenData.toString());

                        if (token == null) {
                                throw new IllegalStateException("無法產生登入 Token");
                        }

                        LoginResponseDTO loginResult = new LoginResponseDTO(
                                        token,
                                        user.account(),
                                        user.userId(),
                                        user.name(),
                                        user.email(),
                                        user.roleCodes(),
                                        user.mustChangePassword());

                        HttpSession session = request.getSession(true);
                        session.setAttribute(
                                        SESSION_ATTRIBUTE,
                                        loginResult);

                        response.sendRedirect(
                                        frontendUrl + "/auth/login?oauth=success");
                } catch (Exception exception) {
                        response.sendRedirect(
                                        frontendUrl + "/auth/login?oauth=failed");
                }
        }
}