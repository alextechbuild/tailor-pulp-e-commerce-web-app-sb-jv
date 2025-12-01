// ----------------------------------------------- Package

package com.tailor_pulp_website.back_end.service;

// ----------------------------------------------- Java Spring Boot

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;




@Service
public class InventoryService {
    

    private final JdbcTemplate jdbcTemplate;


    @Autowired
    public InventoryService(JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }




    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getProductSizeAndQuantity(int productId) {

        try {

            List<Map<String, Object>> inventoryRequest = jdbcTemplate.queryForList("SELECT size, available_quantity FROM inventory WHERE product_id=?", 
                productId
            );

            if (inventoryRequest.size() > 0
            && Arrays.stream(inventoryRequest.toArray()).allMatch((el) -> ( !((Map<String,Object>) el).isEmpty() && Arrays.stream(((Map<String,Object>) el).values().toArray()).allMatch((sub_el) -> (sub_el != null)) ))) {
            
                return inventoryRequest;
            }
            else {

                throw new RuntimeException("some data were not found in request result");
            }
        }
        catch(DataAccessException e) {

            throw new RuntimeException("RuntimeException (getProductSizeAndQuantity) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getAvailableQuantity(int productId, String productSize) {

        try {

            List<Map<String, Object>> inventoryRequest = jdbcTemplate.queryForList("SELECT available_quantity FROM inventory WHERE product_id=? AND size=?", 
                productId, productSize
            );

            if (inventoryRequest.size() > 0
            && Arrays.stream(inventoryRequest.toArray()).allMatch((el) -> ( !((Map<String,Object>) el).isEmpty() && Arrays.stream(((Map<String,Object>) el).values().toArray()).allMatch((sub_el) -> (sub_el != null)) ))) {
            
                return inventoryRequest;
            }
            else {

                throw new RuntimeException("some data were not found in request result");
            }
        }
        catch(DataAccessException e) {

            throw new RuntimeException("RuntimeException (getAvailableQuantity) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    public List<Map<String, Object>> getInventoryID(int productId, String orderedSize) {

        try {

            List<Map<String, Object>> inventoryRequest = jdbcTemplate.queryForList("SELECT inventory_id FROM inventory WHERE product_id=? AND size=?", 
                productId, orderedSize
            );

            if (inventoryRequest.size() == 1 && inventoryRequest.get(0) instanceof Map) {

                if (!(inventoryRequest.get(0).isEmpty() && Arrays.stream(inventoryRequest.get(0).values().toArray()).allMatch(el -> el != null))) {

                    return List.of(inventoryRequest.get(0));
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

            throw new RuntimeException("RuntimeException (getInventoryID) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    public List<Map<String, Object>> updateShoppingCartByProductInfo(int clientId, int productId, String orderedSize, int orderedQuantity, int inventoryId) {

        try {

            List<Map<String, Object>> shoppingCartsRequest = jdbcTemplate.queryForList("SELECT * FROM shopping_carts WHERE product_id=? AND product_size=?", 
                productId, orderedSize
            );

            if (shoppingCartsRequest.size() == 0) {

                jdbcTemplate.update("INSERT INTO shopping_carts (client_id, product_id, product_size, product_quantity, inventory_id) VALUES (?, ?, ?, ?, ?)", 
                    clientId, productId, orderedSize, orderedQuantity, inventoryId
                );

                return List.of(Map.of("message", "Product added to shopping cart"));
            }
            else {

                if (shoppingCartsRequest.size() != 0) {

                    return List.of(Map.of("message", "Product with the same size already added to the shopping cart"));
                }
                else {

                    return List.of(Map.of("message", "Empty request"));
                }
            }
        }
        catch(DataAccessException e) {

            throw new RuntimeException("RuntimeException (updateShoppingCartByProductInfo) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getProductInfoByCategory(String Category) {

        try {

            List<Map<String, Object>> request = jdbcTemplate.queryForList("SELECT * FROM products WHERE category=?", 
                Category
            );

            if (request.size() > 0
            && Arrays.stream(request.toArray()).allMatch((el) -> ( !((Map<String,Object>) el).isEmpty() && Arrays.stream(((Map<String,Object>) el).values().toArray()).allMatch((sub_el) -> (sub_el != null)) ))) {
            
                return request;
            }
            else {

                throw new RuntimeException("Empty request");
            }
        }
        catch(DataAccessException e) {

            throw new RuntimeException("RuntimeException (getProductInfoByCategory) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }
}
