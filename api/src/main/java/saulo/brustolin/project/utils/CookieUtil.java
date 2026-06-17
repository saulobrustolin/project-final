package saulo.brustolin.project.utils;

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
}
