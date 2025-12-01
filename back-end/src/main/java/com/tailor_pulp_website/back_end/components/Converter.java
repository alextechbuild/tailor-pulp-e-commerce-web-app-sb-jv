// ----------------------------------------------- Package

package com.tailor_pulp_website.back_end.components;

// ----------------------------------------------- Java Spring Boot

import org.springframework.stereotype.Component;




@Component
public class Converter {
    

    public static int ObjectToID(Object obj) {

        return ((obj instanceof Number) ? ((Number) obj).intValue() : Integer.parseInt(obj.toString()));
    }


    public static long ObjectToPaymentNumber(Object obj) {

        return ((obj instanceof Number) ? ((Number) obj).longValue() : Long.parseLong(obj.toString()));
    }


    public static int ObjectToQuantity(Object obj) {

        return ((obj instanceof Number) ? ((Number) obj).intValue() : Integer.parseInt(obj.toString()));
    }
}
