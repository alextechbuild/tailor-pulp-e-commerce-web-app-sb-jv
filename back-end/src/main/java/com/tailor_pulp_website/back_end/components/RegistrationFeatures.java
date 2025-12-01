// ----------------------------------------------- Package

package com.tailor_pulp_website.back_end.components;

// ----------------------------------------------- Java Spring Boot

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;




@Component
public class RegistrationFeatures {
    



    public Map<String, Object> fetchRecaptchaGoogleRes(String RECAPTCHA_SECRET_KEY, String reCaptchaToken) {

        try {

            String oauth2URL = "https://www.google.com/recaptcha/api/siteverify?secret=" + RECAPTCHA_SECRET_KEY + "&response=" + reCaptchaToken;


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

            throw new RuntimeException("RuntimeException (fetchRecaptchaGoogleRes) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }
}
