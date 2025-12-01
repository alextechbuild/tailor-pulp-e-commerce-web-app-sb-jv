// ----------------------------------------------- Package

package com.tailor_pulp_website.back_end.interceptor;

// ----------------------------------------------- Java Spring Boot

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.security.Key;

import com.tailor_pulp_website.back_end.components.AppConfig;
import com.tailor_pulp_website.back_end.components.AuthenticationSign;




@Component
public class AuthenticationInterceptor implements HandlerInterceptor {
    

    private final AppConfig appConfig;
    private final AuthenticationSign authenticationSign;


    @Autowired
    public AuthenticationInterceptor(AppConfig appConfig, AuthenticationSign authenticationSign) {

        this.appConfig = appConfig;
        this.authenticationSign = authenticationSign;
    }




    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        try {

            // Récupération du body depuis CleanInterceptor. Si body est null, on récupère le body et on le parse, sinon on passe directement aux cookies et JWT
            if (request.getAttribute("body") != null) {

                InputStream requestInputstream = request.getInputStream();

                Reader requestInputstreamReader = new InputStreamReader(requestInputstream, StandardCharsets.UTF_8);

                BufferedReader requestInputstreamReaderBuffer = new BufferedReader(requestInputstreamReader);


                StringBuilder extractedBody = new StringBuilder();
                String line;

                while ((line = requestInputstreamReaderBuffer.readLine()) != null) {

                    extractedBody.append(line);
                }

                // Si la requête n'est pas GET ou DELETE
                if (!extractedBody.toString().isEmpty()) {

                    ObjectMapper extractedBodyMapper = new ObjectMapper();
                    Map<String, Object> body = extractedBodyMapper.readValue(extractedBody.toString(), new TypeReference<Map<String, Object>>() {});

                    request.setAttribute("body", body);
                }
            }




            // Vérification cookie et token JWT + création payload token JWT

            String cookieValue = null;
            Cookie[] cookies = request.getCookies();

            if (cookies != null) {

                Cookie cookie = Arrays.stream(cookies).filter((el) -> ("token".equals(el.getName()))).findFirst().orElse(null);

                String parts[] = cookie.getValue().substring(2).split("\\.");

                if (parts.length == 2) {

                    cookieValue = parts[0];
                    String tag = parts[1];

                    String cookieHMACTag = authenticationSign.getCookieHMACTag(appConfig.getCookieSecret(), cookieValue);

                    if (tag.equals(cookieHMACTag)) {

                        request.setAttribute("signedCookies", cookieValue); // On a besoin uniquement du token (toujours encodé car pas de vérification)
                    }
                    else {

                        request.setAttribute("signedCookies", null);
                    }
                }
                else {

                    request.setAttribute("signedCookies", null);
                }
            }
            else {

                request.setAttribute("signedCookies", null);
            }

            
            String extractedToken = null;

            if (request.getAttribute("signedCookies") != null) {

                extractedToken = (String) request.getAttribute("signedCookies");
            }
            else if (request.getHeader("Authorization") != null) {

                extractedToken = request.getHeader("Authorization").split(" ")[1];
            }


            if (extractedToken != null) {

                Key JWTSecretKey = new SecretKeySpec(appConfig.getJwtSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");

                Claims tokenPayload = Jwts.parserBuilder()
                .setSigningKey(JWTSecretKey)
                .build()
                .parseClaimsJws(authenticationSign.decodeEncodedJWTToken(extractedToken)) // On décode le token uniquement lors de la vérification
                .getBody();

                request.setAttribute("payload", tokenPayload);
            }
            else {

                // Payload à null car on estime que le reset password se fait côté authentification donc le reset password ne nécessite pas de payload 
                // côté contrôleur et comme d'autres fonctionnalités nécessitent payload côté contrôleur on refera donc des tests du payload côté contrôleur
                request.setAttribute("payload", null);
            }

            return true;
        }
        catch(Exception e) {

            System.out.println("RuntimeException (AuthenticationInterceptor) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
            return false;
        }
    }
}
