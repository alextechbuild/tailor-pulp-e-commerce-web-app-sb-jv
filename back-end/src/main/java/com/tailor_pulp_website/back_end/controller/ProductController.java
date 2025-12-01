// ----------------------------------------------- Package

package com.tailor_pulp_website.back_end.controller;

// ----------------------------------------------- Java Spring Boot

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tailor_pulp_website.back_end.components.Converter;
import com.tailor_pulp_website.back_end.service.InventoryService;

import jakarta.servlet.http.HttpServletRequest;

import io.jsonwebtoken.Claims;




@RestController
@RequestMapping("/product")
public class ProductController {
    

    private List<Map<String, Object>> inventoryServiceResult;
    private Map<String, Object> response;

    private final InventoryService inventoryService;


    @Autowired
    public ProductController(InventoryService inventoryService) {

        this.inventoryService = inventoryService;
    }


    public void setInventoryServiceResult(List<Map<String, Object>> inventoryServiceResult) {

        this.inventoryServiceResult = inventoryServiceResult;
    }


    public List<Map<String, Object>> getInventoryServiceResult() {

        return this.inventoryServiceResult;
    }


    public void setResponse(Map<String, Object> response) {

        this.response = response;
    }


    public Map<String, Object> getResponse() {

        return this.response;
    }




    @SuppressWarnings("unchecked")
    @GetMapping("/getSizesAndQuantities")
    public ResponseEntity<Map<String, Object>> getSizesAndQuantities(HttpServletRequest request) {

        try {

            if (request.getAttribute("cleanedQuery") != null) {

                if (((Map<String, String>) request.getAttribute("cleanedQuery")).get("productId") != "") {

                    int productId = Integer.parseInt(((Map<String, String>) request.getAttribute("cleanedQuery")).get("productId"));
                    setInventoryServiceResult(inventoryService.getProductSizeAndQuantity(productId));

                    setResponse(Map.of("message", getInventoryServiceResult()));
                    return ResponseEntity.ok().body(getResponse());
                }
                else {

                    setResponse(Map.of("message", (Object) "productId is null"));
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




    @SuppressWarnings("unchecked")
    @GetMapping("/getProductQuantity")
    public ResponseEntity<Map<String, Object>> getProductQuantity(HttpServletRequest request) {

        try {

            if (request.getAttribute("cleanedQuery") != null) {

                String productSize = ((Map<String, String>) request.getAttribute("cleanedQuery")).get("productSize");

                if (((Map<String, String>) request.getAttribute("cleanedQuery")).get("productId") != "" && productSize != "") {

                    int productId = Integer.parseInt(((Map<String, String>) request.getAttribute("cleanedQuery")).get("productId"));

                    setInventoryServiceResult(inventoryService.getAvailableQuantity(productId, productSize));

                    setResponse(Map.of("message", getInventoryServiceResult()));
                    return ResponseEntity.ok().body(getResponse());
                }
                else {

                    setResponse(Map.of("message", (Object) "productId or productSize is null"));
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(getResponse());
                }
            }
            else {

                setResponse(Map.of("message", (Object) "cleanedQuery is null"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(getResponse());
            }
        }
        catch(Exception e) {

            System.out.println(e.getMessage() + "\n" + e.getStackTrace()[0]);
            setResponse(Map.of("message", (Object) e.getMessage() + "\n" + e.getStackTrace()[0]));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(getResponse());
        }
    }




    @PostMapping("/AddProductToShoppingCart")
    public ResponseEntity<Map<String, Object>> addProductToShoppingCart(HttpServletRequest request) {

        try {

            Claims tokenPayload = (Claims) request.getAttribute("payload");

            if (tokenPayload != null) {

                if (tokenPayload.get("clientId") != null) {

                    int clientId = Converter.ObjectToID(tokenPayload.get("clientId"));

                    @SuppressWarnings("unchecked")
                    Map<String, Object> body = (Map<String, Object>) request.getAttribute("body");

                    if (body != null) {

                        if (body.get("productId") != null && body.get("orderedSize") != null && body.get("orderedQuantity") != null) {

                            int productId = Converter.ObjectToID(body.get("productId"));
                            String orderedSize = (String) body.get("orderedSize");
                            int orderedQuantity = Converter.ObjectToQuantity(body.get("orderedQuantity"));

                            setInventoryServiceResult(inventoryService.getInventoryID(productId, orderedSize));

                            int inventoryId = Converter.ObjectToID(getInventoryServiceResult().get(0).get("inventory_id"));

                            setInventoryServiceResult(inventoryService.updateShoppingCartByProductInfo(clientId, productId, orderedSize, orderedQuantity, inventoryId));

                            String message = (String) getInventoryServiceResult().get(0).get("message");

                            if (message.equals("Product added to shopping cart")) {

                                setResponse(Map.of("message", message));
                                return ResponseEntity.ok().body(getResponse());
                            }
                            else {

                                setResponse(Map.of("message", message));
                                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(getResponse());
                            }
                        }
                        else {

                            setResponse(Map.of("message", (Object) "productId or orderedSize or orderedQuantity is null"));
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(getResponse());
                        }
                    }
                    else {

                        setResponse(Map.of("message", (Object) "body is null"));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(getResponse());
                    }
                }
                else {

                    setResponse(Map.of("message", (Object) "clientId is null"));
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(getResponse());
                }
            }
            else {

                setResponse(Map.of("message", (Object) "payload not found in front_end_request /me"));
                return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(getResponse());
            }
        }
        catch(Exception e) {

            System.out.println(e.getMessage() + "\n" + e.getStackTrace()[0]);
            setResponse(Map.of("message", (Object) e.getMessage() + "\n" + e.getStackTrace()[0]));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(getResponse());
        }
    }
}
