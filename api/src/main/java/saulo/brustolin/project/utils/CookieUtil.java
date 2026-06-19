package saulo.brustolin.project.utils;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletResponse;

@Component
public class CookieUtil {
    
    private final Integer TIME_EXPIRED;

    public CookieUtil(@Value("${api.security.token.expired}") Integer timeExpired) {
        this.TIME_EXPIRED = timeExpired;
    }

    public void addingCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(TIME_EXPIRED)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void removeCookie(HttpServletResponse response) throws IOException {
        ResponseCookie deleteCookie = ResponseCookie.from("accessToken", "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\": \"Sessão expirada ou inválida. Faça login novamente.\"}");
    }
}
