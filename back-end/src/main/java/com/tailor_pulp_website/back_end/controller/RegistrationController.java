// ----------------------------------------------- Package

package com.tailor_pulp_website.back_end.controller;

// ----------------------------------------------- Java Spring Boot

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.tailor_pulp_website.back_end.components.AppConfig;
import com.tailor_pulp_website.back_end.components.Converter;
import com.tailor_pulp_website.back_end.components.Cryptography;
import com.tailor_pulp_website.back_end.components.RegistrationFeatures;
import com.tailor_pulp_website.back_end.components.Sanitize;
import com.tailor_pulp_website.back_end.service.ClientsService;

import jakarta.servlet.http.HttpServletRequest;




@RestController
@RequestMapping("/registration")
public class RegistrationController {
    

    private Map<String, Object> response;
    private Map<String, Object> clientsServiceResult;


    private final RegistrationFeatures registrationFeatures;
    private final AppConfig appConfig;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final Cryptography cryptography;
    private final ClientsService clientsService;


    @Autowired
    public RegistrationController(
        RegistrationFeatures registrationFeatures, 
        AppConfig appConfig, 
        BCryptPasswordEncoder bCryptPasswordEncoder, 
        Cryptography cryptography, 
        ClientsService clientsService
    ) {

        this.registrationFeatures = registrationFeatures;
        this.appConfig = appConfig;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.cryptography = cryptography;
        this.clientsService = clientsService;
    }


    public Map<String, Object> getClientsServiceResult() {

        return this.clientsServiceResult;
    }


    public void setClientsServiceResult(Map<String, Object> clientsServiceResult) {

        this.clientsServiceResult = clientsServiceResult;
    }


    public void setResponse(Map<String, Object> response) {

        this.response = response;
    }


    public Map<String, Object> getResponse() {

        return this.response;
    }




    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(HttpServletRequest request) {

        try {

            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) request.getAttribute("body");

            if (body != null) {

                String clientEmail = (String) body.get("clientEmail");
                String clientPassword = (String) body.get("clientPassword");
                String clientFirstName = (String) body.get("clientFirstName");
                String clientLastName = (String) body.get("clientLastName");
                String clientCountry = (String) body.get("clientCountry");
                String clientPhoneNumber = (String) body.get("clientPhoneNumber");
                String clientAddress = Sanitize.sanitizeText((String) body.get("clientAddress"));
                String clientTown = (String) body.get("clientTown");
                String clientPostCode = (String) body.get("clientPostCode");
                String clientBirthYear = (String) body.get("clientBirthYear");
                String reCaptchaToken = (String) body.get("reCaptchaToken");
                boolean newsletterSubscription = (boolean) body.get("newsletterSubscription");


                Map<String, Object> google_result = registrationFeatures.fetchRecaptchaGoogleRes(appConfig.getRecaptchaSecretKey(), reCaptchaToken);

                if (google_result.get("success") != null) {

                    String hashedPassword = bCryptPasswordEncoder.encode(clientPassword);

                    if (clientLastName != null) clientLastName = cryptography.encryptPayload(clientLastName, appConfig.getPublicKey());
                    if (clientPhoneNumber != null) clientPhoneNumber = cryptography.encryptPayload(clientPhoneNumber, appConfig.getPublicKey());
                    if (clientAddress != null) clientAddress = cryptography.encryptPayload(clientAddress, appConfig.getPublicKey());
                    if (clientTown != null) clientTown = cryptography.encryptPayload(clientTown, appConfig.getPublicKey());
                    if (clientBirthYear != null) clientBirthYear = cryptography.encryptPayload(clientBirthYear, appConfig.getPublicKey());


                    clientsService.insertNewClient(clientEmail, hashedPassword, clientFirstName, clientLastName, clientPhoneNumber, clientAddress, clientTown, clientPostCode, clientCountry, clientBirthYear);


                    if (newsletterSubscription) {

                        setClientsServiceResult(clientsService.getClientIDByEmail(clientEmail));

                        int clientId = Converter.ObjectToID(getClientsServiceResult().get("client_id"));

                        clientsService.subscribeClientToNewsletter(clientId);

                        setResponse(Map.of("message", (Object) "Account created and client successfully subscribed to newsletter"));
                        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(getResponse());
                    }
                    else {

                        setResponse(Map.of("message", (Object) "Account created"));
                        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(getResponse());
                    }
                }
                else {

                    setResponse(Map.of("message", (Object) "Captcha not matching"));
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
                }
            }
            else {

                setResponse(Map.of("message", (Object) "body is null"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
            }
        }
        catch(Exception e) {

            System.out.println(e.getMessage() + "\n" + e.getStackTrace()[0]);
            setResponse(Map.of("message", (Object) e.getMessage() + "\n" + e.getStackTrace()[0]));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
        }
    }
}
