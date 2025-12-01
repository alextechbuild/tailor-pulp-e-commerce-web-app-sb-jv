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

import com.tailor_pulp_website.back_end.service.InventoryService;

import jakarta.servlet.http.HttpServletRequest;




@RestController
@RequestMapping("/firstLoading")
public class FirstLoadingController {
    

    private List<Map<String, Object>> inventoryServiceResult;
    private Map<String, Object> response;

    private final InventoryService inventoryService;


    @Autowired
    public FirstLoadingController(InventoryService inventoryService) {

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




    @GetMapping("/loadImgs")
    public ResponseEntity<Map<String, Object>> loadImgs(HttpServletRequest request) {

        try {

            if (request.getAttribute("cleanedQuery") != null) {

                @SuppressWarnings("unchecked")
                String Category = ((Map<String, String>) request.getAttribute("cleanedQuery")).get("Category");

                if (Category != null && !Category.equals("")) {

                    setInventoryServiceResult(inventoryService.getProductInfoByCategory(Category));

                    setResponse(Map.of("message", getInventoryServiceResult()));
                    return ResponseEntity.ok().body(getResponse());
                }
                else {

                    setResponse(Map.of("message", (Object) "Category is empty"));
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
}
