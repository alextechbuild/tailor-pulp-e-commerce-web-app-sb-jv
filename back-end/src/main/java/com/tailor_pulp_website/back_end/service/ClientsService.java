// ----------------------------------------------- Package

package com.tailor_pulp_website.back_end.service;

// ----------------------------------------------- Java Spring Boot

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.tailor_pulp_website.back_end.components.Cryptography;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;




@Service
public class ClientsService {
    

    private final JdbcTemplate jdbcTemplate;
    private final Cryptography cryptography;


    @Autowired
    public ClientsService(JdbcTemplate jdbcTemplate, Cryptography cryptography) {

        this.jdbcTemplate = jdbcTemplate;
        this.cryptography = cryptography;
    }




    public Map<String, Object> getUserByEmail_Login(String clientEmail) {

        try {

            List<Map<String, Object>> clientsRequest = jdbcTemplate.queryForList("SELECT * FROM clients WHERE email=?", 
                clientEmail
            );

            if (clientsRequest.size() == 1 && clientsRequest.get(0) instanceof Map) {

                if (!clientsRequest.get(0).isEmpty()) {

                    return clientsRequest.get(0);
                }
                else {

                    throw new RuntimeException("some data were null in request result");
                }
            }
            else {

                throw new RuntimeException("some data were not found in request result");
            }
        }
        catch(DataAccessException e) {

            throw new RuntimeException("RuntimeException (getUserByEmail_Login) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    public Map<String, Object> getTwoFASecret_Login(int clientId) {

        try {

            List<Map<String, Object>> clientsRequest = jdbcTemplate.queryForList("SELECT twofa_secret FROM clients WHERE client_id=?", 
                clientId
            );

            if (clientsRequest.size() == 1 && clientsRequest.get(0) instanceof Map) {

                if (!(clientsRequest.get(0).isEmpty() && Arrays.stream(clientsRequest.get(0).values().toArray()).allMatch(el -> el != null))) {

                    if (clientsRequest.get(0).containsKey("twofa_secret")) {

                        return clientsRequest.get(0);
                    }
                    else {

                        throw new RuntimeException("twofa_secret is null in request result");
                    }
                }
                else {

                    throw new RuntimeException("some data were null in request result");
                }
            }
            else {

                throw new RuntimeException("some data were not found in request result");
            }
        }
        catch(DataAccessException e) {

            throw new RuntimeException("RuntimeException (getTwoFASecret_Login) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    public String checkDBClientEmailPresenceWithGoogleClientEmail_GoogleLogin(String googleClientEmail) {

        try {

            List<Map<String, Object>> clientsRequest = jdbcTemplate.queryForList("SELECT email FROM clients WHERE email=?", 
                googleClientEmail
            );

            if (clientsRequest.size() == 0) {

                return "clientEmail not in db yet";
            }
            else {

                return "clientEmail already in db";
            }
        }
        catch(DataAccessException e) {

            throw new RuntimeException("RuntimeException (checkDBClientEmailPresenceWithGoogleClientEmail_GoogleLogin) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    public int insertNewClientWithGoogleInfoOnly_GoogleLogin(String clientGoogleEmail, String clientGoogleFirstName, String clientEncryptedLastName) {

        try {

            return jdbcTemplate.update("INSERT INTO clients (email, password, first_name, last_name, phone_number, address, town, postcode, country, birth_year, registration_year) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", 
                clientGoogleEmail, "", clientGoogleFirstName, clientEncryptedLastName, "", "", "", "", "", "", LocalDate.now().getYear()
            );
        }
        catch(DataAccessException e) {

            throw new RuntimeException("RuntimeException (insertNewClientWithGoogleInfoOnly_GoogleLogin) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    public Map<String, Object> getClientIDAndClientEmail_GoogleLogin(String clientGoogleEmail, String clientGoogleFirstName) {

        try {

            List<Map<String, Object>> clientsRequest = jdbcTemplate.queryForList("SELECT client_id, email FROM clients WHERE email=? AND first_name=?", 
                clientGoogleEmail, clientGoogleFirstName
            );

            if (clientsRequest.size() == 1 && clientsRequest.get(0) instanceof Map) {

                if (!(clientsRequest.get(0).isEmpty() && Arrays.stream(clientsRequest.get(0).values().toArray()).allMatch(el -> el != null))) {

                    if (clientsRequest.get(0).containsKey("client_id") && clientsRequest.get(0).containsKey("email")) {

                        return clientsRequest.get(0);
                    }
                    else {

                        throw new RuntimeException("client_id or email is null in request result");
                    }
                }
                else {

                    throw new RuntimeException("some data were null in request result");
                }
            }
            else {

                throw new RuntimeException("some data were not found in request result");
            }
        }
        catch(DataAccessException e) {

            throw new RuntimeException("RuntimeException (getClientIDAndClientEmail_GoogleLogin) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    public Map<String, Object> getActiveClientFeatures_Me(int clientId) {

        try {

            List <Map<String, Object>> clientsRequest = jdbcTemplate.queryForList("SELECT is_2fa_enabled, is_subscribed_to_newsletter FROM clients WHERE client_id=?", 
                clientId
            );

            if (clientsRequest.size() == 1 && clientsRequest.get(0) instanceof Map) {

                if (!(clientsRequest.get(0).isEmpty() && Arrays.stream(clientsRequest.get(0).values().toArray()).allMatch(el -> el != null))) {

                    if (clientsRequest.get(0).containsKey("is_2fa_enabled") && clientsRequest.get(0).containsKey("is_subscribed_to_newsletter")) {

                        return clientsRequest.get(0);
                    }
                    else {

                        throw new RuntimeException("is_2fa_enabled or is_subscribed_to_newsletter is null in request result");
                    }
                }
                else {

                    throw new RuntimeException("some data were null in request result");
                }
            }
            else {

                throw new RuntimeException("some data were not found in request result");
            }
        }
        catch(DataAccessException e) {

            throw new RuntimeException("RuntimeException (getActiveClientFeatures_Me) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    public int deleteClientFromDB_Signout(int clientId) {

        try {

            return jdbcTemplate.update("DELETE FROM clients WHERE client_id=?", 
                clientId
            );
        }
        catch(DataAccessException e) {

            throw new RuntimeException("RuntimeException (deleteClientFromDB_Signout) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    public Map<String, Object> getClientIDByEmail(String Email) {

        try {

            List<Map<String, Object>> clientsRequest = jdbcTemplate.queryForList("SELECT client_id FROM clients WHERE email=?", 
                Email
            );

            if (clientsRequest.size() == 1 && clientsRequest.get(0) instanceof Map) {

                if (!(clientsRequest.get(0).isEmpty() && Arrays.stream(clientsRequest.get(0).values().toArray()).allMatch(el -> el != null))) {

                    if (clientsRequest.get(0).containsKey("client_id")) {

                        return clientsRequest.get(0);
                    }
                    else {

                        throw new RuntimeException("client_id is null in request result");
                    }
                }
                else {

                    throw new RuntimeException("some data were null in request result");
                }
            }
            else {

                throw new RuntimeException("some data were not found in request result");
            }
        }
        catch(DataAccessException e) {

            throw new RuntimeException("RuntimeException (getClientIDByEmail) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    public int insertModifiedClientPasswordByID(String encryptedClientNewPassword, int clientID) {

        try {

            return jdbcTemplate.update("UPDATE clients SET password=? WHERE client_id=?", 
                encryptedClientNewPassword, clientID
            );
        }
        catch(DataAccessException e) {

            throw new RuntimeException("RuntimeException (insertModifiedClientPasswordByID) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    public int updateTwoFATempSecretByID(String twoFATempSecret, int clientId) {

        try {

            return jdbcTemplate.update("UPDATE clients SET twofa_temp_secret=? WHERE client_id=?", 
                twoFATempSecret, clientId
            );
        }
        catch(DataAccessException e) {

            throw new RuntimeException("RuntimeException (updateTwoFATempSecretByID) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    public int enableClient2FAByID(String secret, int clientId) {

        try {

            return jdbcTemplate.update("UPDATE clients SET (is_2fa_enabled, twofa_secret, twofa_temp_secret)=(true, ?, NULL) WHERE client_id=? AND is_2fa_enabled=false", 
                secret, clientId
            );
        }
        catch(DataAccessException e) {

            throw new RuntimeException("RuntimeException (enableClient2FAByID) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    public int disableClient2FAByID(int clientId) {

        try {

            return jdbcTemplate.update("UPDATE clients SET (is_2fa_enabled, twofa_secret)=(false, NULL) WHERE client_id=? AND is_2fa_enabled=true", 
                clientId
            );
        }
        catch(DataAccessException e) {

            throw new RuntimeException("RuntimeException (disableClient2FAByID) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    @SuppressWarnings("unchecked")
    public Map<String, List<Map<String, Object>>> getClientShoppingCart(int clientId, String privateKey) {

        try {

            List<Map<String, Object>> clientsRequest = jdbcTemplate.queryForList("SELECT email, first_name, last_name, phone_number, address, town, postcode, country, is_subscribed_to_newsletter FROM clients WHERE client_id=?", 
                clientId
            );


            if (clientsRequest.size() == 1 && clientsRequest.get(0) instanceof Map) {

                if (!(clientsRequest.get(0).isEmpty() && Arrays.stream(clientsRequest.get(0).values().toArray()).allMatch(el -> el != null))) {

                    String clientEmail = (String) clientsRequest.get(0).get("email");
                    String clientFirstName = (String) clientsRequest.get(0).get("first_name");
                    String clientLastName = (String) clientsRequest.get(0).get("last_name");
                    String clientPhoneNumber = (String) clientsRequest.get(0).get("phone_number");
                    String clientAddress = (String) clientsRequest.get(0).get("address");
                    String clientTown = (String) clientsRequest.get(0).get("town");
                    String clientPostcode = (String) clientsRequest.get(0).get("postcode");
                    String clientCountry = (String) clientsRequest.get(0).get("country");
                    boolean isSubscribedToNewsletter = (boolean) clientsRequest.get(0).get("is_subscribed_to_newsletter");
                    
                    String decryptedClientLastName = cryptography.decryptPayload(clientLastName, privateKey, 1);
                    String decryptedClientPhoneNumber = cryptography.decryptPayload(clientPhoneNumber, privateKey, 2);
                    String decryptedClientAddress = cryptography.decryptPayload(clientAddress, privateKey, 3).replace("_", " ");
                    String decryptedClientTown = cryptography.decryptPayload(clientTown, privateKey, 4);


                    Map<String, Object> plaintextClientInformation = new LinkedHashMap<>();
                    plaintextClientInformation.put("email", clientEmail);
                    plaintextClientInformation.put("first_name", clientFirstName);
                    plaintextClientInformation.put("last_name", decryptedClientLastName);
                    plaintextClientInformation.put("phone_number", decryptedClientPhoneNumber);
                    plaintextClientInformation.put("address", decryptedClientAddress);
                    plaintextClientInformation.put("town", decryptedClientTown);
                    plaintextClientInformation.put("postcode", clientPostcode);
                    plaintextClientInformation.put("country", clientCountry);
                    plaintextClientInformation.put("newsletter", ((isSubscribedToNewsletter) ? "subscribed" : "not subscribed"));

                    List<Map<String, Object>> clientList = new ArrayList<>();
                    clientList.addFirst(plaintextClientInformation);




                    List<Map<String, Object>> shoppingCartsRequest = jdbcTemplate.queryForList(
                    """
                    SELECT sub_request.shopping_cart_id, sub_request.product_id, sub_request.product_name, sub_request.product_size, sub_request.product_quantity, i.unit_price, sub_request.image_path, STRING_AGG(i.size, ',') AS available_sizes, STRING_AGG(CAST(i.available_quantity AS VARCHAR(256)), ',') AS available_quantities
                    FROM inventory AS i 
                    INNER JOIN (
                        SELECT s.shopping_cart_id, s.product_id, p.name AS product_name, s.product_size, s.product_quantity, p.image_path 
                        FROM shopping_carts AS s 
                        INNER JOIN products AS p ON p.product_id=s.product_id 
                        WHERE client_id=?
                    ) AS sub_request
                    ON sub_request.product_id=i.product_id
                    WHERE i.product_id IN (
                        SELECT s.product_id 
                        FROM shopping_carts AS s 
                        WHERE s.client_id=?
                    ) 
                    AND 
                    (
                    (
                    SELECT COUNT(inv.size)
                    FROM inventory AS inv
                    WHERE inv.product_id=sub_request.product_id
                    )=1
                    OR
                    i.size NOT IN (
                        SELECT s.product_size 
                        FROM shopping_carts AS s 
                        WHERE s.client_id=?
                    )
                    )
                    GROUP BY sub_request.shopping_cart_id, sub_request.product_id, sub_request.product_name, sub_request.product_size, sub_request.product_quantity, i.unit_price, sub_request.image_path 
                    """, 
                        clientId, clientId, clientId
                    );


                    if (shoppingCartsRequest.size() > 0
                    && Arrays.stream(shoppingCartsRequest.toArray()).allMatch((el) -> ( !((Map<String,Object>) el).isEmpty() && Arrays.stream(((Map<String,Object>) el).values().toArray()).allMatch((sub_el) -> (sub_el != null)) ))) {
                    
                        Map<String, List<Map<String, Object>>> obj = new LinkedHashMap<>();
                        obj.put("client", clientList);
                        obj.put("product", shoppingCartsRequest);

                        return obj;
                    }
                    else {

                        Map<String, List<Map<String, Object>>> obj = new LinkedHashMap<>();
                        obj.put("client", clientList);

                        return obj;
                    }
                }
                else {

                    throw new RuntimeException("some data were null in request result");
                }
            }
            else {

                throw new RuntimeException("some data were not found in request result");
            }            
        }
        catch(DataAccessException e) {

            throw new RuntimeException("RuntimeException (getClientShoppingCart) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    public int subscribeClientToNewsletter(int clientId) {

        try {

            return jdbcTemplate.update("UPDATE clients SET is_subscribed_to_newsletter=true WHERE client_id=?", clientId);
        }
        catch(DataAccessException e) {

            throw new RuntimeException("RuntimeException (subscribeClientToNewsletter) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    public int unsubscribeClientToNewsletter(int clientId) {

        try {

            return jdbcTemplate.update("UPDATE clients SET is_subscribed_to_newsletter=false WHERE client_id=?", clientId);
        }
        catch(DataAccessException e) {

            throw new RuntimeException("RuntimeException (unsubscribeClientToNewsletter) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    public int updateNewClientInfo(String clientFirstName, String clientLastName, String clientPhoneNumber, String clientAddress, String clientTown, String clientPostCode, String clientCountry, int clientId) {

        try {

            return jdbcTemplate.update(
                """
                UPDATE clients SET 
                first_name=COALESCE(NULLIF(?, ''), first_name),
                last_name=COALESCE(NULLIF(?, ''), last_name),
                phone_number=COALESCE(NULLIF(?, ''), phone_number),
                address=COALESCE(NULLIF(?, ''), address),
                town=COALESCE(NULLIF(?, ''), town),
                postcode=COALESCE(NULLIF(?, ''), postcode),
                country=COALESCE(NULLIF(?, ''), country)
                WHERE
                client_id=?    
                """, 
                clientFirstName, clientLastName, clientPhoneNumber, clientAddress, clientTown, clientPostCode, clientCountry, clientId
            );
        }
        catch(DataAccessException e) {

            throw new RuntimeException("RuntimeException (updateNewClientInfo) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }



    
    public int insertNewClient(String clientEmail, String hashedPassword, String clientFirstName, String encryptedLastName, String encryptedPhoneNumber, String encryptedAddress, String encryptedTown, String clientPostCode, String clientCountry, String encryptedBirthYear) {

        try {

            return jdbcTemplate.update("INSERT INTO clients (email, password, first_name, last_name, phone_number, address, town, postcode, country, birth_year, registration_year) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", 
                clientEmail, hashedPassword, clientFirstName, encryptedLastName, encryptedPhoneNumber, encryptedAddress, encryptedTown, clientPostCode, clientCountry, encryptedBirthYear, LocalDate.now().getYear()
            );
        }
        catch(DataAccessException e) {

            throw new RuntimeException("RuntimeException (insertNewClientWithGoogleInfoOnly_GoogleLogin) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getClientIDsForNewsletter() {

        try {

            List<Map<String, Object>> clientsRequest = jdbcTemplate.queryForList("SELECT client_id FROM clients WHERE is_subscribed_to_newsletter=true");

            if (clientsRequest.size() > 0
            && Arrays.stream(clientsRequest.toArray()).allMatch((el) -> ( !((Map<String,Object>) el).isEmpty() && Arrays.stream(((Map<String,Object>) el).values().toArray()).allMatch((sub_el) -> (sub_el != null)) ))) {
            
                return clientsRequest;
            }
            else {

                throw new RuntimeException("Empty request");
            }
        }
        catch(DataAccessException e) {

            throw new RuntimeException("RuntimeException (getClientIDsForNewsletter) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }
}
