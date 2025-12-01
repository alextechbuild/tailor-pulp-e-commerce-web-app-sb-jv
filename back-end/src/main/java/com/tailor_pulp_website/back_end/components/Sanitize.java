// ----------------------------------------------- Package

package com.tailor_pulp_website.back_end.components;

// ----------------------------------------------- Java Spring Boot

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;




@Component
public class Sanitize {
    



    public static void sanitizeHTML(HttpServletRequest request) {

        try {

            if (request.getAttribute("body") != null) {

                @SuppressWarnings("unchecked")
                Map<String, Object> body = (Map<String, Object>) request.getAttribute("body");

                String[] keysList = body.keySet().toArray(new String[0]);

                Map<String, Object> cleanedBody = new HashMap<>();

                for (String key : keysList) {

                    Object value = body.get(key);

                    if (value instanceof String) {

                        cleanedBody.put(key, Jsoup.clean((String) body.get(key), Safelist.none()));
                    }
                    else {

                        cleanedBody.put(key, value);
                    }
                }

                request.setAttribute("body", cleanedBody);
            }
            else if (request.getParameterMap().size() > 0) {

                Map<String, String> cleanedQuery = new HashMap<>();

                String[] keyList = request.getParameterMap().keySet().toArray(new String[0]);

                for (String key : keyList) {

                    if (request.getParameter(key) != null) cleanedQuery.put(key, Jsoup.clean(request.getParameter(key), Safelist.none()));
                }

                if (cleanedQuery.size() > 0) request.setAttribute("cleanedQuery", cleanedQuery);
            }
        }
        catch(Exception e) {

            throw new RuntimeException("RuntimeException (sanitizeHTML) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    public static String sanitizeText(String text) {

        return text.replace("\u00A0", " ")
        .replace("\u200B", "")
        .replace("\uFEFF", "")
        .replaceAll("\\s+", " ")
        .trim();
    }
}
