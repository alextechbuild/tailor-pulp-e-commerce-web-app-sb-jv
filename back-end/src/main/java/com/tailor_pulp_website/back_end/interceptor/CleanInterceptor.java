// ----------------------------------------------- Package

package com.tailor_pulp_website.back_end.interceptor;

// ----------------------------------------------- Java Spring Boot

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.tailor_pulp_website.back_end.components.Sanitize;




@Component
public class CleanInterceptor implements HandlerInterceptor {




    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        try {

            // Récupération du body. Si body est null, on récupère le body et on le parse, sinon on passe directement au clean (flexibilité)
            if (request.getAttribute("body") == null) {

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


            Sanitize.sanitizeHTML(request);

            return true;
        }
        catch(Exception e) {

            System.out.println("RuntimeException (CleanInterceptor) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
            return false;
        }
    }
}
