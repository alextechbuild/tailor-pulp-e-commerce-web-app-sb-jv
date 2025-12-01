// ----------------------------------------------- Package

package com.tailor_pulp_website.back_end.components;

// ----------------------------------------------- Java Spring Boot

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.sql.Date;
import java.util.Base64;
import java.util.Map;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;




@Component
public class AuthenticationSign {




    public String getCookieHMACTag(String cookieSecret, String valueToSign) {

        try {

            Key cookieSecretKey = new SecretKeySpec(cookieSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

            // Déclaration du MAC en MAC de type HMAC
            Mac mac = Mac.getInstance("HmacSHA256");
            // Initialisation du HMAC
            mac.init(cookieSecretKey);

            // Tag d'authentification
            String tag = Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(valueToSign.getBytes(StandardCharsets.UTF_8)));

            return tag;
        }
        catch(Exception e) {

            throw new RuntimeException("RuntimeException (getCookieHMACTag) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    public String getJWTToken(String JwtSecretKey, int clientId, String clientEmail) {

        try {

            Key JWTSecretKey = new SecretKeySpec(JwtSecretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

            String token = Jwts.builder()
            .setExpiration(new Date(System.currentTimeMillis() + (15 * 60 * 1000)))
            .claim("clientId", Integer.toString(clientId))
            .claim("clientEmail", clientEmail)
            .claim("role", "user")
            .signWith(JWTSecretKey)
            .compact();

            return token;
        }
        catch(Exception e) {

            throw new RuntimeException("RuntimeException (getJWTToken) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    public String encodeJWTToken(String JwtToken) {

        return Base64.getUrlEncoder().withoutPadding().encodeToString(JwtToken.getBytes(StandardCharsets.UTF_8));
    }




    public String decodeEncodedJWTToken(String EncodedJwtToken) {

        return new String(Base64.getUrlDecoder().decode(EncodedJwtToken), StandardCharsets.UTF_8);
    }




    public String getSignedCookieHeader(String name, String value, String signature, int maxAge) {

        try {

            String signedCookieHeader = String.format(

                "%s=s:%s.%s; Path=/; HttpOnly; Secure; SameSite=None; Max-Age=%d",
                name,
                value,
                signature,
                maxAge
            );

            return signedCookieHeader;
        }
        catch(Exception e) {

            throw new RuntimeException("RuntimeException (getSignedCookieHeader) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    public Map<String, Object> fetchOAuthGoogleRes(String Credential) {

        try {

            String oauth2URL = "https://oauth2.googleapis.com/tokeninfo?id_token=" + Credential;


            HttpRequest backEndRequest = HttpRequest.newBuilder()
            .uri(URI.create(oauth2URL))
            .GET()
            .build();


            HttpClient httpClient = HttpClient.newHttpClient();
            HttpResponse<String> googleResponse = httpClient.send(backEndRequest, HttpResponse.BodyHandlers.ofString());
            String googleResponseExtractedBody = googleResponse.body();


            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> response = objectMapper.readValue(googleResponseExtractedBody, new TypeReference<Map<String, Object>>() {});


            return response;
        }
        catch(Exception e) {

            throw new RuntimeException("RuntimeException (fetchOAuthGoogleRes) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    public String getClearedSignedCookieHeader(String name, String value, String signature) {

        try {

            String signedCookieHeader = String.format(

                "%s=s:%s.%s; Path=/; HttpOnly; Secure; SameSite=None; Max-Age=%d",
                name,
                value,
                signature,
                0
            );

            return signedCookieHeader;
        }
        catch(Exception e) {

            throw new RuntimeException("RuntimeException (getSignedCookieHeader) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    public boolean verifyTOTPSecret(String secretToVerify, String tokenToVerify, int window) {

        try {

            GoogleAuthenticatorConfig config = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
            .setWindowSize(window)
            .build();

            GoogleAuthenticator authenticator = new GoogleAuthenticator(config);

            return authenticator.authorize(secretToVerify, Integer.parseInt(tokenToVerify));
        }
        catch(Exception e) {

            throw new RuntimeException("RuntimeException (verifyTOTPSecret) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }
}
