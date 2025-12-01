// ----------------------------------------------- Package

package com.tailor_pulp_website.back_end.controller;

import java.io.ByteArrayOutputStream;

// ----------------------------------------------- Java Spring Boot

import java.util.Base64;
import java.util.HashMap;

import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base32;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.tailor_pulp_website.back_end.service.ClientsService;
import com.tailor_pulp_website.back_end.service.SMTPService;
import com.tailor_pulp_website.back_end.service.TokensService;

import jakarta.servlet.http.HttpServletRequest;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.tailor_pulp_website.back_end.components.AppConfig;
import com.tailor_pulp_website.back_end.components.AuthenticationSign;
import com.tailor_pulp_website.back_end.components.Converter;
import com.tailor_pulp_website.back_end.components.Cryptography;
import io.jsonwebtoken.Claims;

import java.security.Key;




@RestController
@RequestMapping("/authentication")
public class AuthenticationController {
    

    private final ClientsService clientsService;
    private final TokensService tokensService;
    private final SMTPService smtpService;

    private final BCryptPasswordEncoder bcryptPasswordEncoder;
    private final AppConfig appConfig;
    private final AuthenticationSign authenticationSign;
    private final Cryptography cryptography;

    private Map<String, Object> clientsServiceResult;
    private Map<String, Object> tokensServiceResult;
    private Map<String, Object> response;


    @Autowired
    public AuthenticationController(
        ClientsService clientsService, 
        TokensService tokensService,
        SMTPService smtpService,
        BCryptPasswordEncoder bcryptPasswordEncoder, 
        AppConfig appConfig, 
        AuthenticationSign authenticationSign,
        Cryptography cryptography
    ) {

        this.clientsService = clientsService;
        this.tokensService = tokensService;
        this.smtpService = smtpService;
        this.bcryptPasswordEncoder = bcryptPasswordEncoder;
        this.appConfig = appConfig;
        this.authenticationSign = authenticationSign;
        this.cryptography = cryptography;
    }




    public Map<String, Object> getClientsServiceResult() {

        return this.clientsServiceResult;
    }


    public void setClientsServiceResult(Map<String, Object> clientsServiceResult) {

        this.clientsServiceResult = clientsServiceResult;
    }


    public Map<String, Object> getTokensServiceResult() {

        return this.tokensServiceResult;
    }


    public void setTokensServiceResult(Map<String, Object> tokensServiceResult) {

        this.tokensServiceResult = tokensServiceResult;
    }


    public Map<String, Object> getResponse() {

        return this.response;
    }


    public void setResponse(Map<String, Object> response) {

        this.response = response;
    }




    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(HttpServletRequest request) {

        try {

            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) request.getAttribute("body");


            if (body != null) {


                String userEmail = (String) body.get("clientEmail");
                String clientPassword = (String) body.get("clientPassword");


                if (userEmail != null || clientPassword != null) {

                    setClientsServiceResult(clientsService.getUserByEmail_Login(userEmail));


                    int clientId = Converter.ObjectToID(getClientsServiceResult().get("client_id"));
                    String clientEmail = (String) getClientsServiceResult().get("email");
                    String encryptedPassword = (String) getClientsServiceResult().get("password");


                    boolean isClientPasswordChecked = bcryptPasswordEncoder.matches(clientPassword, encryptedPassword);


                    if (isClientPasswordChecked) {


                        setClientsServiceResult(clientsService.getTwoFASecret_Login(clientId));

                        String twofa_secret = (String) getClientsServiceResult().get("twofa_secret");

                        if (twofa_secret != null && !twofa_secret.isEmpty()) {

                            Map<String, Object> obj = new HashMap<>();
                            obj.put("twofa_enabled", true);

                            setResponse(Map.of("message", obj));
                            return ResponseEntity.ok().body(getResponse());
                        }
                        else {


                            String token = authenticationSign.encodeJWTToken(authenticationSign.getJWTToken(appConfig.getJwtSecret(), clientId, clientEmail));


                            if (!token.isEmpty()) {


                                String signedCookieHeader = authenticationSign.getSignedCookieHeader("token", token, authenticationSign.getCookieHMACTag(appConfig.getCookieSecret(), token), 15 * 60); // 15 minutes en secondes


                                HttpHeaders cookieHeaders = new HttpHeaders();
                                cookieHeaders.add("Set-Cookie", signedCookieHeader);

                                Map<String, Object> obj = new HashMap<>();
                                obj.put("token", token);
                                obj.put("clientFirstName", clientEmail.split("@")[0]);

                                setResponse(Map.of("message", obj));
                                return ResponseEntity.ok().headers(cookieHeaders).body(getResponse());
                            }
                            else {

                                setResponse(Map.of("message", (Object) "token not found"));
                                return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).contentType(MediaType.APPLICATION_JSON).body(getResponse());
                            }
                        }                 
                    }
                    else {

                        setResponse(Map.of("message", (Object) "password not found"));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
                    }
                }
                else {

                    setResponse(Map.of("message", (Object) "userEmail or clientPassword is null"));
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




    @PostMapping("/GoogleLogin")
    public ResponseEntity<Map<String, Object>> googleLogin(HttpServletRequest request) {

        try {

            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) request.getAttribute("body");


            if (body != null) {

                String Credential = (String) body.get("Credential");


                if (Credential != null) {


                    Map<String, Object> result = authenticationSign.fetchOAuthGoogleRes(Credential);
                    String isEmailVerified = (result.containsKey("email_verified")) ? (String) result.get("email_verified") : null;


                    if (isEmailVerified != null && isEmailVerified.equals("true")) {

                        
                        String aud = (result.containsKey("aud")) ? (String) result.get("aud") : null;

                        if (aud != null && aud.equals(appConfig.getGoogleOAuthClientID())) {


                            String clientGoogleEmail = (result.containsKey("email")) ? (String) result.get("email") : null;
                            String clientGoogleFirstName = (result.containsKey("given_name")) ? (String) result.get("given_name") : null;
                            String clientGoogleLastName = (result.containsKey("family_name")) ? (String) result.get("family_name") : null;


                            if (clientGoogleEmail != null && clientGoogleFirstName != null && clientGoogleLastName != null) {


                                String clientEncryptedLastName = cryptography.encryptPayload(clientGoogleLastName, appConfig.getPublicKey());


                                if (clientsService.checkDBClientEmailPresenceWithGoogleClientEmail_GoogleLogin(clientGoogleEmail).equals("clientEmail not in db yet")) {

                                    // Pas NULL car permet d'ajouter de la flexibilité pour les tests de requête (centralisation de la chaîne vide pour comptes Google et pour pouvoir insérer de nouvelles infos directement côté front éventuellement)
                                    clientsService.insertNewClientWithGoogleInfoOnly_GoogleLogin(clientGoogleEmail, clientGoogleFirstName, clientEncryptedLastName);
                                }


                                setClientsServiceResult(clientsService.getClientIDAndClientEmail_GoogleLogin(clientGoogleEmail, clientGoogleFirstName));

                                int clientId = Converter.ObjectToID(getClientsServiceResult().get("client_id"));
                                String clientEmail = (String) getClientsServiceResult().get("email");

                                String token = authenticationSign.encodeJWTToken(authenticationSign.getJWTToken(appConfig.getJwtSecret(), clientId, clientEmail));


                                if (!token.isEmpty()) {


                                    String signedCookieHeader = authenticationSign.getSignedCookieHeader("token", token, authenticationSign.getCookieHMACTag(appConfig.getCookieSecret(), token), 15 * 60); // 15 minutes en secondes


                                    HttpHeaders cookieHeaders = new HttpHeaders();
                                    cookieHeaders.add("Set-Cookie", signedCookieHeader);


                                    Map<String, Object> obj = new HashMap<>();
                                    obj.put("token", token);
                                    obj.put("clientFirstName", clientEmail.split("@")[0]);


                                    setResponse(Map.of("message", obj));
                                    return ResponseEntity.ok().headers(cookieHeaders).body(getResponse());
                                }
                                else {

                                    setResponse(Map.of("message", (Object) "token not found"));
                                    return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).contentType(MediaType.APPLICATION_JSON).body(getResponse());
                                }
                            }
                            else {

                                setResponse(Map.of("message", (Object) "clientEmail or clientFirstName or clientLastName is null"));
                                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
                            }
                        }
                        else {

                            setResponse(Map.of("message", (Object) "GoogleLogin : token not intended for this application"));
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
                        }
                    }
                    else {

                        setResponse(Map.of("message", (Object) "GoogleLogin : email not verified by Google"));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
                    }
                }
                else {

                    setResponse(Map.of("message", (Object) "Credential is null"));
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




    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(HttpServletRequest request) {

        try {

            // Si l'utilisateur refresh depuis la page Login, le useState associé au token n'est pas trouvable donc n'est pas dans le header authorization
            // Donc on vérifie uniquement la présence du token dans le cookie
            Claims tokenPayload = (Claims) request.getAttribute("payload");

            if (tokenPayload != null) {

                if (tokenPayload.get("clientId") != null && tokenPayload.get("clientEmail") != null) {

                    int clientId = Converter.ObjectToID(tokenPayload.get("clientId"));
                    String clientEmail = (String) tokenPayload.get("clientEmail");


                    setClientsServiceResult(clientsService.getActiveClientFeatures_Me(clientId));

                    boolean is2FAEnabled = (boolean) getClientsServiceResult().get("is_2fa_enabled");
                    boolean isSubscribedToNewsletter = (boolean) getClientsServiceResult().get("is_subscribed_to_newsletter");

                    String token = (String) request.getAttribute("signedCookies");


                    Map<String, Object> obj = new HashMap<>();
                    obj.put("token", token);
                    obj.put("clientFirstName", clientEmail.split("@")[0]);
                    obj.put("is2FAEnabled", is2FAEnabled);
                    obj.put("isSubscribedToNewsletter", isSubscribedToNewsletter);


                    setResponse(Map.of("message", obj));
                    // Tant que tu ne renvoies pas de Set-Cookie qui modifie ou supprime des cookies initialisés, ils restent inchangés côté navigateur
                    return ResponseEntity.ok().body(getResponse());
                }
                else {

                    setResponse(Map.of("message", (Object) "clientId or clientEmail is null"));
                    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(getResponse());
                }
            }
            else {

                setResponse(Map.of("message", (Object) "Token not found"));
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(getResponse());
            }
        }
        catch(Exception e) {

            System.out.println(e.getMessage() + "\n" + e.getStackTrace()[0]);
            setResponse(Map.of("message", (Object) e.getMessage() + "\n" + e.getStackTrace()[0]));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
        }
    }




    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {

        try {

            String extractedToken = (request.getAttribute("signedCookies") != null) ? (String) request.getAttribute("signedCookies") : request.getHeader("Authorization").split(" ")[1];

            if (extractedToken != null) {

                String clearedSignedCookieHeader = authenticationSign.getClearedSignedCookieHeader("token", extractedToken, authenticationSign.getCookieHMACTag(appConfig.getCookieSecret(), extractedToken));

                HttpHeaders cookieHeaders = new HttpHeaders();
                cookieHeaders.add("Set-Cookie", clearedSignedCookieHeader);

                setResponse(Map.of("message", (Object) "Logout successful"));
                return ResponseEntity.ok().headers(cookieHeaders).contentType(MediaType.APPLICATION_JSON).body(getResponse());
            }
            else {

                setResponse(Map.of("message", (Object) "Logout successful"));
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(getResponse());
            }
        }
        catch(Exception e) {

            System.out.println(e.getMessage() + "\n" + e.getStackTrace()[0]);
            setResponse(Map.of("message", (Object) e.getMessage() + "\n" + e.getStackTrace()[0]));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
        }
    }




    @DeleteMapping("/signout")
    public ResponseEntity<Map<String, Object>> signout(HttpServletRequest request) {

        try {

            Claims tokenPayload = (Claims) request.getAttribute("payload");

            if (tokenPayload != null) {

                if (tokenPayload.get("clientId") != null && tokenPayload.get("clientEmail") != null) {

                    int clientId = Converter.ObjectToID(tokenPayload.get("clientId"));

                    clientsService.deleteClientFromDB_Signout(clientId);
                    
                    setResponse(Map.of("message", (Object) "Signout successful"));
                    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(getResponse());
                }
                else {

                    setResponse(Map.of("message", (Object) "clientId or clientEmail is null"));
                    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(getResponse());
                }
            }
            else {

                setResponse(Map.of("message", (Object) "Payload not found"));
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(getResponse());
            }
        }
        catch(Exception e) {

            System.out.println(e.getMessage() + "\n" + e.getStackTrace()[0]);
            setResponse(Map.of("message", (Object) e.getMessage() + "\n" + e.getStackTrace()[0]));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
        }
    }




    @GetMapping("/sendVerificationCode")
    public ResponseEntity<Map<String, Object>> sendVerificationCode(HttpServletRequest request) {

        try {

            if (request.getAttribute("cleanedQuery") != null) {

                @SuppressWarnings("unchecked")
                String Email = ((Map<String, String>) request.getAttribute("cleanedQuery")).get("Email"); // Extraction paramètre URL par paramètre URL pour query

                if (Email != "") {

                    String resetPasswordToken = Base64.getEncoder().encodeToString(cryptography.getRandomBytes());

                    setClientsServiceResult(clientsService.getClientIDByEmail(Email));
                    int clientId = (int) getClientsServiceResult().get("client_id");


                    tokensService.insertResetPasswordTokenInfo(clientId, resetPasswordToken);

                    smtpService.sendEmail(

                        String.format("Tailor Pulp Company <%s>", appConfig.getHostEmail()),
                        appConfig.getHostEmail(),
                        "(Password Reset) Your Temporary Verification Code",
                        String.format(
                        """
                        You asked us to reset your password account.
                        \nHere is your temporary verification code :\t %s
                        \nIt will expires in >>5 MINUTES<<.
                        \n\n
                        Please do not reply. This is an automated message.
                        \nFor any information or assistance, please contact us at the following email address: %s
                        """
                        , resetPasswordToken, appConfig.getAssistanceEmail()),
                        false
                    );

                    setResponse(Map.of("message", (Object) "Message sent if the account exists"));
                    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(getResponse());
                }
                else {

                    setResponse(Map.of("message", (Object) "Email is empty"));
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
                }
            }
            else {

                setResponse(Map.of("message", (Object) "cleanedQuery is null"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
            }
        }
        catch(Exception e) {

            System.out.println(e.getMessage() + "\n" + e.getStackTrace()[0]);
            setResponse(Map.of("message", (Object) e.getMessage() + "\n" + e.getStackTrace()[0]));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
        }
    }




    @PatchMapping("/modifyClientPassword")
    public ResponseEntity<Map<String, Object>> modifyClientPassword(HttpServletRequest request) {

        try {

            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) request.getAttribute("body");

            if (body != null) {

                String Email = (String) body.get("Email");
                String code = (String) body.get("code");
                String clientNewPassword = (String) body.get("clientNewPassword");

                if (Email != null && code != null && clientNewPassword != null) {

                    setClientsServiceResult(clientsService.getClientIDByEmail(Email));

                    int clientId = Converter.ObjectToID(getClientsServiceResult().get("client_id"));

                    setTokensServiceResult(tokensService.getResetPasswordTokenInfo(code));

                    String encryptedClientNewPassword = bcryptPasswordEncoder.encode(clientNewPassword);

                    clientsService.insertModifiedClientPasswordByID(encryptedClientNewPassword, clientId);

                    setResponse(Map.of("message", (Object) "The client password has been reset successfully"));
                    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(getResponse());
                }
                else {

                    setResponse(Map.of("message", (Object) "Email or code or clientNewPassword is empty"));
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




    @GetMapping("/generateTOTPSecret")
    public ResponseEntity<Map<String, Object>> generateTOTPSecret(HttpServletRequest request) {

        try {

            Claims tokenPayload = (Claims) request.getAttribute("payload");

            if (tokenPayload != null) {

                if (tokenPayload.get("clientId") != null && tokenPayload.get("clientEmail") != null) {

                    int clientID = Converter.ObjectToID(tokenPayload.get("clientId"));
                    String clientEmail = (String) tokenPayload.get("clientEmail");

                    Key secretKey = new SecretKeySpec(cryptography.getRandomBytes(), "HmacSHA256");
                    String totpSecret = new Base32().encodeAsString(secretKey.getEncoded());

                    clientsService.updateTwoFATempSecretByID(totpSecret, clientID);


                    String appName = "TailorPulp";
                    String issuer = "TailorPulpClient";

                    String otpauthURL = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", 
                        appName,
                        clientEmail,
                        totpSecret,
                        issuer
                    );

                    QRCodeWriter qrCodeWriter = new QRCodeWriter();
                    BitMatrix encodedQrCode = qrCodeWriter.encode(otpauthURL, BarcodeFormat.QR_CODE, 250, 250);

                    ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
                    MatrixToImageWriter.writeToStream(encodedQrCode, "PNG", pngOutputStream);
                    byte[] pngData = pngOutputStream.toByteArray();

                    String dataURL = "data:image/png;base64," + Base64.getEncoder().encodeToString(pngData);


                    Map<String, Object> obj = new HashMap<>();
                    obj.put("dataURL", dataURL);
                    obj.put("secretBase32", totpSecret);


                    setResponse(Map.of("message", obj));
                    return ResponseEntity.ok().body(getResponse());
                }
                else {

                    setResponse(Map.of("message", (Object) "clientId or clientEmail is null"));
                    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(getResponse());
                }
            }
            else {

                setResponse(Map.of("message", (Object) "Payload not found"));
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(getResponse());
            }
        }
        catch(Exception e) {

            System.out.println(e.getMessage() + "\n" + e.getStackTrace()[0]);
            setResponse(Map.of("message", (Object) e.getMessage() + "\n" + e.getStackTrace()[0]));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
        }
    }




    @PostMapping("/enable-2fa")
    public ResponseEntity<Map<String, Object>> enable2FA(HttpServletRequest request) {

        try {

            Claims tokenPayload = (Claims) request.getAttribute("payload");

            if (tokenPayload != null) {

                if (tokenPayload.get("clientId") != null) {

                    int clientId = Converter.ObjectToID(tokenPayload.get("clientId"));

                    @SuppressWarnings("unchecked")
                    Map<String, Object> body = (Map<String, Object>) request.getAttribute("body");

                    if (body != null) {

                        String code = (String) body.get("code");
                        String secret = (String) body.get("secret");

                        if (code != null && secret != null) {

                            boolean isTOTPCodeMatching = authenticationSign.verifyTOTPSecret(secret, code, 10);

                            if (isTOTPCodeMatching) {

                                clientsService.enableClient2FAByID(secret, clientId);

                                setResponse(Map.of("message", (Object) "2fa enabled successfully"));
                                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(getResponse());
                            }
                            else {

                                setResponse(Map.of("message", (Object) "TOTP Code not matching"));
                                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
                            }
                        }
                        else {

                            setResponse(Map.of("message", (Object) "code or secret is null"));
                            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(getResponse());
                        }
                    }
                    else {

                        setResponse(Map.of("message", (Object) "body is null"));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
                    }
                }
                else {

                    setResponse(Map.of("message", (Object) "clientId is null"));
                    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(getResponse());
                }
            }
            else {

                setResponse(Map.of("message", (Object) "Payload not found"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
            }
        }
        catch(Exception e) {

            System.out.println(e.getMessage() + "\n" + e.getStackTrace()[0]);
            setResponse(Map.of("message", (Object) e.getMessage() + "\n" + e.getStackTrace()[0]));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
        }
    }




    @DeleteMapping("/disable-2fa")
    public ResponseEntity<Map<String, Object>> disable2FA(HttpServletRequest request) {

        try {

            Claims tokenPayload = (Claims) request.getAttribute("payload");

            if (tokenPayload != null) {

                if (tokenPayload.get("clientId") != null) {

                    int clientId = Converter.ObjectToID(tokenPayload.get("clientId"));

                    if (request.getAttribute("cleanedQuery") != null) {

                        @SuppressWarnings("unchecked")
                        String code = ((Map<String, String>) request.getAttribute("cleanedQuery")).get("code");

                        if (code != "") {

                            setClientsServiceResult(clientsService.getTwoFASecret_Login(clientId));
                            String totpSecret = (String) getClientsServiceResult().get("twofa_secret");

                            boolean isTOTPCodeMatching = authenticationSign.verifyTOTPSecret(totpSecret, code, 2);

                            if (isTOTPCodeMatching) {

                                clientsService.disableClient2FAByID(clientId);

                                setResponse(Map.of("message", (Object) "2fa disabled successfully"));
                                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(getResponse());
                            }
                            else {

                                setResponse(Map.of("message", (Object) "TOTP Code not matching"));
                                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
                            }
                        }
                        else {

                            setResponse(Map.of("message", (Object) "code is empty"));
                            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(getResponse());
                        }
                    }
                    else {

                        setResponse(Map.of("message", (Object) "cleanedQuery is null"));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
                    }
                }
                else {

                    setResponse(Map.of("message", (Object) "clientId is null"));
                    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(getResponse());
                }
            }
            else {

                setResponse(Map.of("message", (Object) "Payload not found"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
            }
        }
        catch(Exception e) {

            System.out.println(e.getMessage() + "\n" + e.getStackTrace()[0]);
            setResponse(Map.of("message", (Object) e.getMessage() + "\n" + e.getStackTrace()[0]));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
        }
    }




    @PostMapping("/confirm-2fa")
    public ResponseEntity<Map<String, Object>> confirm2FA(HttpServletRequest request) {

        try {

            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) request.getAttribute("body");

            if (body != null) {

                String clientEmail = (String) body.get("clientEmail");
                String clientPassword = (String) body.get("clientPassword");
                String code = (String) body.get("code");

                if (clientEmail != null && clientPassword != null && code != null) {

                    setClientsServiceResult(clientsService.getUserByEmail_Login(clientEmail));
                    String clientEncryptedPassword = (String) getClientsServiceResult().get("password");
                    int clientId = Converter.ObjectToID(getClientsServiceResult().get("client_id"));

                    boolean isClientPasswordChecked = bcryptPasswordEncoder.matches(clientPassword, clientEncryptedPassword);

                    if (isClientPasswordChecked) {

                        setClientsServiceResult(clientsService.getTwoFASecret_Login(clientId));
                        String totpSecret = (String) getClientsServiceResult().get("twofa_secret");

                        boolean isTOTPCodeMatching = authenticationSign.verifyTOTPSecret(totpSecret, code, 2);

                        if (isTOTPCodeMatching) {

                            String token = authenticationSign.encodeJWTToken(authenticationSign.getJWTToken(appConfig.getJwtSecret(), clientId, clientEmail));

                            if (!token.isEmpty()) {

                                String signedCookieHeader = authenticationSign.getSignedCookieHeader("token", token, authenticationSign.getCookieHMACTag(appConfig.getCookieSecret(), token), 15 * 60);


                                HttpHeaders cookieHeaders = new HttpHeaders();
                                cookieHeaders.add("Set-Cookie", signedCookieHeader);

                                Map<String, Object> obj = new HashMap<>();
                                obj.put("token", token);
                                obj.put("clientFirstName", clientEmail.split("@")[0]);

                                setResponse(Map.of("message", obj));
                                return ResponseEntity.ok().headers(cookieHeaders).body(getResponse());
                            }
                            else {

                                setResponse(Map.of("message", (Object) "token not found"));
                                return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).contentType(MediaType.APPLICATION_JSON).body(getResponse());
                            }
                        }
                        else {

                            setResponse(Map.of("message", (Object) "TOTP Code not matching"));
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
                        }
                    }
                    else {

                        setResponse(Map.of("message", (Object) "password not found"));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
                    }
                }
                else {

                    setResponse(Map.of("message", (Object) "clientEmail or clientPassword or code is null"));
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
