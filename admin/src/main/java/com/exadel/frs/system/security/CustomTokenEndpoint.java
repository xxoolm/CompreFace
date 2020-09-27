package com.exadel.frs.system.security;

import com.exadel.frs.entity.User;
import java.security.Principal;
import java.util.Map;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping(value = "/oauth/token")
public class CustomTokenEndpoint extends TokenEndpoint {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "CFSESSION";

    @PostMapping
    public ResponseEntity<OAuth2AccessToken> postAccessToken(
            Principal principal,
            @RequestParam Map<String, String> parameters
    ) throws HttpRequestMethodNotSupportedException {

        if (principal instanceof UsernamePasswordAuthenticationToken) {
            if (((UsernamePasswordAuthenticationToken) principal).getPrincipal() instanceof User) {
                return ResponseEntity.status(HttpStatus.OK).build();
            }
        }

        ResponseEntity<OAuth2AccessToken> defaultResponse = super.postAccessToken(principal, parameters);
        OAuth2AccessToken defaultToken = defaultResponse.getBody();

        HttpCookie cookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, defaultToken.getValue())
                                          .httpOnly(true)
                                          .maxAge(defaultToken.getExpiresIn())
                                          .path("/admin")
                                          .build();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.status(HttpStatus.OK).headers(headers).build();
    }
}
