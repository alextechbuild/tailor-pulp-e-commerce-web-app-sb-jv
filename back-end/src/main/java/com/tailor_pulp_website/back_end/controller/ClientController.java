// ----------------------------------------------- Package

package com.tailor_pulp_website.back_end.controller;

// ----------------------------------------------- Java Spring Boot

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tailor_pulp_website.back_end.service.ClientsService;
import com.tailor_pulp_website.back_end.service.SMTPService;
import com.tailor_pulp_website.back_end.service.ShoppingCartService;

import jakarta.servlet.http.HttpServletRequest;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.tailor_pulp_website.back_end.components.AppConfig;
import com.tailor_pulp_website.back_end.components.Converter;
import com.tailor_pulp_website.back_end.components.Cryptography;
import io.jsonwebtoken.Claims;

import java.time.Instant;




@RestController
@RequestMapping("/client")
public class ClientController {


    private final ClientsService clientsService;
    private final AppConfig appConfig;
    private final SMTPService smtpService;
    private final Cryptography cryptography;
    private final ShoppingCartService shoppingCartService;


    private Map<String, List<Map<String, Object>>> clientsServiceResult;
    private Map<String, Object> shoppingCartServiceResult;
    private Map<String, Object> response;
    private Map<String, Integer> EmailHistory;


    @Autowired
    public ClientController(
        ClientsService clientsService, 
        AppConfig appConfig, 
        SMTPService smtpService, 
        Cryptography cryptography, 
        ShoppingCartService shoppingCartService
    ) {

        this.clientsService = clientsService;
        this.appConfig = appConfig;
        this.smtpService = smtpService;
        this.cryptography = cryptography;
        this.shoppingCartService = shoppingCartService;
        this.EmailHistory = new HashMap<>();
    }


    public void setClientsServiceResult(Map<String, List<Map<String, Object>>> clientsServiceResult) {

        this.clientsServiceResult = clientsServiceResult;
    }


    public Map<String, List<Map<String, Object>>> getClientsServiceResult() {

        return this.clientsServiceResult;
    }


    public void setShoppingCartServiceResult(Map<String, Object> shoppingCartServiceResult) {

        this.shoppingCartServiceResult = shoppingCartServiceResult;
    }


    public Map<String, Object> getShoppingCartServiceResult() {

        return this.shoppingCartServiceResult;
    }


    public void setResponse(Map<String, Object> response) {

        this.response = response;
    }


    public Map<String, Object> getResponse() {

        return this.response;
    }


    public Map<String, Integer> getEmailHistory() {

        return this.EmailHistory;
    }


    public void setEmailHistoryNewDateByEmail(String Email, int date) {

        this.EmailHistory.put(Email, date);
    }




    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(HttpServletRequest request) {

        try {

            Claims tokenPayload = (Claims) request.getAttribute("payload");

            if (tokenPayload != null) {

                if (tokenPayload.get("clientId") != null) {

                    int clientId = Converter.ObjectToID(tokenPayload.get("clientId"));

                    setClientsServiceResult(clientsService.getClientShoppingCart(clientId, appConfig.getPrivateKey()));
                    Map<String, List<Map<String, Object>>> clientShoppingCart = getClientsServiceResult();

                    if (clientShoppingCart.size() > 0) {

                        setResponse(Map.of("message", clientShoppingCart));
                        return ResponseEntity.ok().body(getResponse());
                    }
                    else {

                        setResponse(Map.of("message", clientShoppingCart));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(getResponse());
                    }
                }
                else {

                    setResponse(Map.of("message", (Object) "clientId is null"));
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
                }
            }
            else {

                setResponse(Map.of("message", (Object) "payload not found in front_end_request /me"));
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(getResponse());
            }
        }
        catch(Exception e) {

            System.out.println(e.getMessage() + "\n" + e.getStackTrace()[0]);
            setResponse(Map.of("message", (Object) e.getMessage() + "\n" + e.getStackTrace()[0]));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
        }
    }




    @PostMapping("/sendFormMessage")
    public ResponseEntity<Map<String, Object>> sendFormMessage(HttpServletRequest request) {

        try {

            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) request.getAttribute("body");

            if (body != null) {

                String Email = (String) body.get("Email");
                String Message = (String) body.get("Message");

                if (Email != null && Message != null) {

                    if (getEmailHistory().get(Email) != null) {

                        if (Instant.now().getEpochSecond() - getEmailHistory().get(Email) < 24 * 60 * 60) {

                            setResponse(Map.of("message", (Object) "Please wait 24 hours before sending another message."));
                            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(getResponse());
                        }
                        else {

                            setEmailHistoryNewDateByEmail(Email, (int) Instant.now().getEpochSecond());

                            // Remplacer le premier argument HOST_EMAIL par Email et le deuxième argument HOST_EMAIL par ASSISTANCE_EMAIL en mode production
                            smtpService.sendEmail(

                                String.format("Client <%s>", appConfig.getHostEmail()),
                                appConfig.getHostEmail(),
                                "Support Contact Message",
                                Message,
                                true
                            );

                            setResponse(Map.of("message", (Object) "Email successfully sent. We will respond as soon as possible."));
                            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(getResponse());
                        }
                    }
                    else {

                        setEmailHistoryNewDateByEmail(Email, (int) Instant.now().getEpochSecond());

                        // Remplacer le premier argument HOST_EMAIL par Email et le deuxième argument HOST_EMAIL par ASSISTANCE_EMAIL en mode production
                        smtpService.sendEmail(

                            String.format("Client <%s>", appConfig.getHostEmail()),
                            appConfig.getHostEmail(),
                            "Support Contact Message",
                            Message,
                            true
                        );

                        setResponse(Map.of("message", (Object) "Email successfully sent. We will respond as soon as possible."));
                        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(getResponse());
                    }
                }
                else {

                    setResponse(Map.of("message", (Object) "Email or Message is null"));
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




    @PostMapping("/modifyClientInfo")
    public ResponseEntity<Map<String, Object>> modifyClientInfo(HttpServletRequest request) {

        try {

            Claims tokenPayload = (Claims) request.getAttribute("payload");

            if (tokenPayload != null) {

                if (tokenPayload.get("clientId") != null) {

                    int clientId = Converter.ObjectToID(tokenPayload.get("clientId"));

                    @SuppressWarnings("unchecked")
                    Map<String, Object> body = (Map<String, Object>) request.getAttribute("body");

                    if (body != null) {

                        String clientFirstName = (String) body.get("clientFirstName");
                        String clientLastName = (String) body.get("clientLastName");
                        String clientCountry = (String) body.get("clientCountry");
                        String clientPhoneNumber = (String) body.get("clientPhoneNumber");
                        String clientAddress = (String) body.get("clientAddress");
                        String clientTown = (String) body.get("clientTown");
                        String clientPostCode = (String) body.get("clientPostCode");
                        boolean newsletterSubscription = (boolean) body.get("newsletterSubscription");

                        if (clientLastName != null) clientLastName = cryptography.encryptPayload(clientLastName, appConfig.getPublicKey());
                        if (clientPhoneNumber != null) clientPhoneNumber = cryptography.encryptPayload(clientPhoneNumber, appConfig.getPublicKey());
                        if (clientAddress != null) clientAddress = cryptography.encryptPayload(clientAddress, appConfig.getPublicKey());
                        if (clientTown != null) clientTown = cryptography.encryptPayload(clientTown, appConfig.getPublicKey());

                        if (newsletterSubscription) {

                            clientsService.subscribeClientToNewsletter(clientId);
                        }
                        else {

                            clientsService.unsubscribeClientToNewsletter(clientId);
                        }

                        clientsService.updateNewClientInfo(clientFirstName, clientLastName, clientPhoneNumber, clientAddress, clientTown, clientPostCode, clientCountry, clientId);

                        setResponse(Map.of("message", (Object) "Client information modified successfully"));
                        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(getResponse());
                    }
                    else {

                        setResponse(Map.of("message", (Object) "body is null"));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
                    }
                }
                else {

                    setResponse(Map.of("message", (Object) "clientId is null"));
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
                }
            }
            else {

                setResponse(Map.of("message", (Object) "payload not found in front_end_request /me"));
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(getResponse());
            }
        }
        catch(Exception e) {

            System.out.println(e.getMessage() + "\n" + e.getStackTrace()[0]);
            setResponse(Map.of("message", (Object) e.getMessage() + "\n" + e.getStackTrace()[0]));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
        }
    }




    @SuppressWarnings("unchecked")
    @DeleteMapping("/deleteOrder")
    public ResponseEntity<Map<String, Object>> deleteOrder(HttpServletRequest request) {
        
        try {

            Claims tokenPayload = (Claims) request.getAttribute("payload");

            if (tokenPayload != null) {

                if (tokenPayload.get("clientId") != null) {

                    int clientId = Converter.ObjectToID(tokenPayload.get("clientId"));

                    
                    if (request.getAttribute("cleanedQuery") != null) {

                        if (((Map<String, String>) request.getAttribute("cleanedQuery")).get("Id") != "") {

                            int Id = Integer.parseInt(((Map<String, String>) request.getAttribute("cleanedQuery")).get("Id"));

                            shoppingCartService.deleteShoppingCartOrder(Id, clientId);

                            
                            setClientsServiceResult(clientsService.getClientShoppingCart(clientId, appConfig.getPrivateKey()));
                            Map<String, List<Map<String, Object>>> clientShoppingCart = getClientsServiceResult();

                            if (clientShoppingCart.size() > 0) {

                                setResponse(Map.of("message", clientShoppingCart));
                                return ResponseEntity.ok().body(getResponse());
                            }
                            else {

                                setResponse(Map.of("message", clientShoppingCart));
                                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(getResponse());
                            }
                        }
                        else {

                            setResponse(Map.of("message", (Object) "Id (shopping_cart) is empty"));
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
                        }
                    }
                    else {

                        setResponse(Map.of("message", (Object) "cleanedQuery is null"));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
                    }
                }
                else {

                    setResponse(Map.of("message", (Object) "clientId is null"));
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
                }
            }
            else {

                setResponse(Map.of("message", (Object) "payload not found in front_end_request /me"));
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(getResponse());
            }
        }
        catch(Exception e) {

            System.out.println(e.getMessage() + "\n" + e.getStackTrace()[0]);
            setResponse(Map.of("message", (Object) e.getMessage() + "\n" + e.getStackTrace()[0]));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
        }
    }




    @PutMapping("/modifyOrder")
    public ResponseEntity<Map<String, Object>> modifyOrder(HttpServletRequest request) {

        try {

            Claims tokenPayload = (Claims) request.getAttribute("payload");

            if (tokenPayload != null) {

                if (tokenPayload.get("clientId") != null) {

                    int clientId = Converter.ObjectToID(tokenPayload.get("clientId"));

                    @SuppressWarnings("unchecked")
                    Map<String, Object> body = (Map<String, Object>) request.getAttribute("body");

                    if (body != null) {

                        if (body.get("shoppingCartId") != null && body.get("newOrderedProductSize") != null && body.get("newOrderedProductQuantity") != null) {

                            int shoppingCartId = Converter.ObjectToID(body.get("shoppingCartId"));
                            String newOrderedProductSize = (String) body.get("newOrderedProductSize");
                            int newOrderedProductQuantity = Converter.ObjectToID(body.get("newOrderedProductQuantity"));
                            
                            setShoppingCartServiceResult(shoppingCartService.getProductId(shoppingCartId));

                            int productId = Converter.ObjectToID(getShoppingCartServiceResult().get("product_id"));


                            setShoppingCartServiceResult(shoppingCartService.getClientShoppingCartInfo(clientId, productId, newOrderedProductSize, newOrderedProductQuantity));


                            if ((boolean) getShoppingCartServiceResult().get("isNoProductWithSameSize")) {


                                shoppingCartService.updateClientShoppingCartProductSizeAndQuantity(clientId, shoppingCartId, newOrderedProductSize, newOrderedProductQuantity);

                                setClientsServiceResult(clientsService.getClientShoppingCart(clientId, appConfig.getPrivateKey()));
                                Map<String, List<Map<String, Object>>> clientShoppingCart = getClientsServiceResult();


                                if (clientShoppingCart.size() > 0) {

                                    setResponse(Map.of("message", clientShoppingCart));
                                    return ResponseEntity.ok().body(getResponse());
                                }
                                else {

                                    setResponse(Map.of("message", clientShoppingCart));
                                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(getResponse());
                                }
                            }
                            else {

                                setResponse(Map.of("message", (Object) "Empty request or Product with the same size already added to the shopping cart"));
                                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
                            }
                        }
                        else {

                            setResponse(Map.of("message", (Object) "shoppingCartId or newOrderedProductSize or newOrderedProductQuantity is null"));
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
                        }
                    }
                    else {

                        setResponse(Map.of("message", (Object) "body is null"));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
                    }
                }
                else {

                    setResponse(Map.of("message", (Object) "clientId is null"));
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
                }
            }
            else {

                setResponse(Map.of("message", (Object) "payload not found in front_end_request /me"));
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(getResponse());
            }
        }
        catch(Exception e) {

            System.out.println(e.getMessage() + "\n" + e.getStackTrace()[0]);
            setResponse(Map.of("message", (Object) e.getMessage() + "\n" + e.getStackTrace()[0]));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
        }
    }




    @PutMapping("/deleteAllOrders")
    public ResponseEntity<Map<String, Object>> deleteAllOrders(HttpServletRequest request) {

        try {

            Claims tokenPayload = (Claims) request.getAttribute("payload");

            if (tokenPayload != null) {

                if (tokenPayload.get("clientId") != null) {

                    int clientId = Converter.ObjectToID(tokenPayload.get("clientId"));

                    shoppingCartService.deleteAllClientOrdersById(clientId);


                    setClientsServiceResult(clientsService.getClientShoppingCart(clientId, appConfig.getPrivateKey()));
                    Map<String, List<Map<String, Object>>> clientShoppingCart = getClientsServiceResult();

                    if (clientShoppingCart.size() > 0) {

                        setResponse(Map.of("message", clientShoppingCart));
                        return ResponseEntity.ok().body(getResponse());
                    }
                    else {

                        setResponse(Map.of("message", clientShoppingCart));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(getResponse());
                    }
                }
                else {

                    setResponse(Map.of("message", (Object) "clientId is null"));
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
                }
            }
            else {

                setResponse(Map.of("message", (Object) "payload not found in front_end_request /me"));
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(getResponse());
            }
        }
        catch(Exception e) {

            System.out.println(e.getMessage() + "\n" + e.getStackTrace()[0]);
            setResponse(Map.of("message", (Object) e.getMessage() + "\n" + e.getStackTrace()[0]));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
        }
    }




    @PostMapping("/createCheckoutSession")
    public ResponseEntity<Map<String, Object>> createCheckoutSession(HttpServletRequest request) {

        try {

            Claims tokenPayload = (Claims) request.getAttribute("payload");

            if (tokenPayload != null) {

                @SuppressWarnings("unchecked")
                Map<String, Object> body = (Map<String, Object>) request.getAttribute("body");
                
                if (body != null) {

                    @SuppressWarnings("unchecked")
                    Map<String, List<Map<String, Object>>> clientShoppingCart = (Map<String, List<Map<String, Object>>>) body.get("clientShoppingCart");

                    if (clientShoppingCart != null) {

                        List<Map<String, Object>> clientProductsList = clientShoppingCart.get("product");

                        Stripe.apiKey = appConfig.getStripeSecretKey();

                        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setBillingAddressCollection(SessionCreateParams.BillingAddressCollection.REQUIRED)
                        .setSuccessUrl(String.format("%s/success-page?session_id={CHECKOUT_SESSION_ID}", appConfig.getFrontEndURL()))
                        .setCancelUrl(String.format("%s/cancel-page?session_id={CHECKOUT_SESSION_ID}", appConfig.getFrontEndURL()))
                        .setShippingAddressCollection(
                            SessionCreateParams.ShippingAddressCollection.builder()
                            .addAllowedCountry(SessionCreateParams.ShippingAddressCollection.AllowedCountry.FR)
                            .addAllowedCountry(SessionCreateParams.ShippingAddressCollection.AllowedCountry.BE)
                            .addAllowedCountry(SessionCreateParams.ShippingAddressCollection.AllowedCountry.NL)
                            .addAllowedCountry(SessionCreateParams.ShippingAddressCollection.AllowedCountry.DE)
                            .addAllowedCountry(SessionCreateParams.ShippingAddressCollection.AllowedCountry.AT)
                            .addAllowedCountry(SessionCreateParams.ShippingAddressCollection.AllowedCountry.GB)
                            .build()
                        );

                        for (Map<String, Object> product : clientProductsList) {

                            SessionCreateParams.LineItem.PriceData.ProductData productData = 
                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName((String) product.get("product_name"))
                            .putMetadata("size", (String) product.get("product_size"))
                            .build();

                            SessionCreateParams.LineItem.PriceData priceData = 
                            SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("eur")
                            .setProductData(productData)
                            .setUnitAmount((Converter.ObjectToPaymentNumber(product.get("unit_price"))) * 100)
                            .build();

                            SessionCreateParams.LineItem lineItem = 
                            SessionCreateParams.LineItem.builder()
                            .setPriceData(priceData)
                            .setQuantity(Converter.ObjectToPaymentNumber(product.get("product_quantity")))
                            .build();

                            paramsBuilder.addLineItem(lineItem);
                        }

                        SessionCreateParams params = paramsBuilder.build();

                        Session session = Session.create(params);


                        if (session != null && session.getUrl() != null) {

                            Map<String, Object> obj = new HashMap<>();
                            obj.put("url", session.getUrl());

                            setResponse(Map.of("message", obj));
                            return ResponseEntity.ok().body(getResponse());
                        }
                        else {

                            setResponse(Map.of("message", (Object) "session not found"));
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
                        }
                    }
                    else {

                        setResponse(Map.of("message", (Object) "clientShoppingCart is null"));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
                    }
                }
                else {

                    setResponse(Map.of("message", (Object) "body is null"));
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
                }
            }
            else {

                setResponse(Map.of("message", (Object) "payload not found in front_end_request /me"));
                return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).contentType(MediaType.APPLICATION_JSON).body(getResponse());
            }
        }
        catch(Exception e) {

            System.out.println(e.getMessage() + "\n" + e.getStackTrace()[0]);
            setResponse(Map.of("message", (Object) e.getMessage() + "\n" + e.getStackTrace()[0]));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
        }
    }




    @PostMapping("/unsubscribeToNewsletter")
    public ResponseEntity<Map<String, Object>> unsubscribeToNewsletter(HttpServletRequest request) {

        try {

            Claims tokenPayload = (Claims) request.getAttribute("payload");

            if (tokenPayload != null) {

                if (tokenPayload.get("clientId") != null) {

                    int clientId = Converter.ObjectToID(tokenPayload.get("clientId"));

                    clientsService.unsubscribeClientToNewsletter(clientId);

                    setResponse(Map.of("message", (Object) "Client successfully unsubscribed from newsletter"));
                    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(getResponse());
                }
                else {

                    setResponse(Map.of("message", (Object) "clientId is null"));
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
                }
            }
            else {

                setResponse(Map.of("message", (Object) "payload not found in front_end_request /me"));
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(getResponse());
            }
        }
        catch(Exception e) {

            System.out.println(e.getMessage() + "\n" + e.getStackTrace()[0]);
            setResponse(Map.of("message", (Object) e.getMessage() + "\n" + e.getStackTrace()[0]));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
        }
    }




    @DeleteMapping("/removeOrder")
    public ResponseEntity<Map<String, Object>> removeOrder(HttpServletRequest request) {

        try {

            Claims tokenPayload = (Claims) request.getAttribute("payload");

            if (tokenPayload != null) {

                if (tokenPayload.get("clientId") != null) {

                    int clientId = Converter.ObjectToID(tokenPayload.get("clientId"));

                    setClientsServiceResult(clientsService.getClientShoppingCart(clientId, appConfig.getPrivateKey()));
                    Map<String, List<Map<String, Object>>> clientShoppingCart = getClientsServiceResult();


                    if (clientShoppingCart.get("product").size() > 0) {

                        for (Map<String, Object> productRow : clientShoppingCart.get("product")) {

                            int productId = Converter.ObjectToID(productRow.get("product_id"));
                            String productSize = (String) productRow.get("product_size");

                            shoppingCartService.updateInventoryBasedOnClientOrder(clientId, productId, productSize);

                            shoppingCartService.removeOrderFromShoppingCart(clientId, productId, productSize);
                        }

                        setResponse(Map.of("message", (Object) "Client order successfully removed"));
                        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(getResponse());
                    }
                    else {

                        setResponse(Map.of("message", (Object) "shopping_cart not found"));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
                    }
                }
                else {

                    setResponse(Map.of("message", (Object) "clientId is null"));
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
                }
            }
            else {

                setResponse(Map.of("message", (Object) "payload not found in front_end_request /me"));
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(getResponse());
            }
        }
        catch(Exception e) {

            System.out.println(e.getMessage() + "\n" + e.getStackTrace()[0]);
            setResponse(Map.of("message", (Object) e.getMessage() + "\n" + e.getStackTrace()[0]));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(getResponse());
        }
    }
}
