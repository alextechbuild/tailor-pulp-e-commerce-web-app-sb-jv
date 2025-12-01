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
public class ShoppingCartService {
    
    private final JdbcTemplate jdbcTemplate;


    @Autowired
    public ShoppingCartService(JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }




    public int deleteShoppingCartOrder(int shoppingCartId, int clientId) {

        try {

            return jdbcTemplate.update("DELETE FROM shopping_carts WHERE shopping_cart_id=? AND client_id=?", 
                shoppingCartId, clientId
            );
        }
        catch(DataAccessException e) {

            throw new RuntimeException("RuntimeException (deleteShoppingCartOrder) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    public Map<String, Object> getProductId(int shoppingCartId) {

        try {

            List<Map<String, Object>> shoppingCartsRequest = jdbcTemplate.queryForList("SELECT product_id FROM shopping_carts WHERE shopping_cart_id=?", 
                shoppingCartId
            );

            if (shoppingCartsRequest.size() == 1 && shoppingCartsRequest.get(0) instanceof Map) {

                if (!(shoppingCartsRequest.get(0).isEmpty() && Arrays.stream(shoppingCartsRequest.get(0).values().toArray()).allMatch(el -> el != null))) {

                    return shoppingCartsRequest.get(0);
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

            throw new RuntimeException("RuntimeException (getProductId) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    public Map<String, Object> getClientShoppingCartInfo(int clientId, int product_id, String newOrderedProductSize, int newOrderedProductQuantity) {

        try {

            boolean isNoProductWithSameSize = false;

            List<Map<String, Object>> shoppingCartsRequest = jdbcTemplate.queryForList("SELECT * FROM shopping_carts WHERE client_id=? AND product_id=? AND product_size=?", 
                clientId, product_id, newOrderedProductSize
            );

            if (shoppingCartsRequest.size() == 0 || shoppingCartsRequest.size() == 1) {

                if (shoppingCartsRequest.size() == 1) {

                    List<Map<String, Object>> productQuantityRequest = jdbcTemplate.queryForList("SELECT * FROM shopping_carts WHERE client_id=? AND product_id=? AND product_size=? AND product_quantity=?", 
                        clientId, product_id, newOrderedProductSize, newOrderedProductQuantity
                    );

                    if (productQuantityRequest.size() != 0) {

                        throw new RuntimeException("Product with the same size already added to the shopping cart");
                    }
                    else {

                        isNoProductWithSameSize = true;
                    }
                }
                else {

                    isNoProductWithSameSize = true;
                }
            }
            else {

                if (shoppingCartsRequest.size() > 1) {

                    throw new RuntimeException("Product with the same size already added to the shopping cart");
                }
                else {

                    throw new RuntimeException("Empty request");
                }
            }

            return Map.of("isNoProductWithSameSize", isNoProductWithSameSize);
        }
        catch(DataAccessException e) {

            throw new RuntimeException("RuntimeException (getClientShoppingCartInfo) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    public int updateClientShoppingCartProductSizeAndQuantity(int clientId, int shoppingCartId, String newOrderedProductSize, int newOrderedProductQuantity) {

        try {

            return jdbcTemplate.update(
            """
            UPDATE shopping_carts 
            SET (product_size, product_quantity)=(?, ?)
            WHERE shopping_cart_id=? AND client_id=?;        
            """, 
                newOrderedProductSize, newOrderedProductQuantity, shoppingCartId, clientId
            );
        }
        catch(DataAccessException e) {

            throw new RuntimeException("RuntimeException (updateClientShoppingCartProductSizeAndQuantity) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    public int deleteAllClientOrdersById(int clientId) {

        try {

            return jdbcTemplate.update("DELETE FROM shopping_carts WHERE client_id=?", 
                clientId
            );
        }
        catch(DataAccessException e) {

            throw new RuntimeException("RuntimeException (deleteAllClientOrdersById) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    public int updateInventoryBasedOnClientOrder(int clientId, int productId, String productSize) {

        try {

            return jdbcTemplate.update(
            """
            UPDATE inventory AS inv 
            SET available_quantity=available_quantity-sub_request.quantity
            FROM (
                SELECT s.product_quantity AS quantity 
                FROM shopping_carts AS s
                WHERE s.client_id=? AND s.product_id=? AND s.product_size=?
            ) AS sub_request 
            WHERE inv.available_quantity >= 2 AND sub_request.quantity IS NOT NULL AND sub_request.quantity <> 0 AND inv.product_id=? AND inv.size=?
            """,
                clientId, productId, productSize, productId, productSize
            );
        }
        catch(DataAccessException e) {

            throw new RuntimeException("RuntimeException (updateInventoryBasedOnClientOrder) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    public int removeOrderFromShoppingCart(int clientId, int productId, String productSize) {

        try {

            return jdbcTemplate.update(
            """
            DELETE FROM shopping_carts
            WHERE client_id=? AND product_id=? AND product_size=?
            """, 
                clientId, productId, productSize
            );
        }
        catch(DataAccessException e) {

            throw new RuntimeException("RuntimeException (removeOrderFromShoppingCart) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }
}
