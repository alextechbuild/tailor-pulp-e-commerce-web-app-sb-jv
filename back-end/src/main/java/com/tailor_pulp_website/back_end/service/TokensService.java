// ----------------------------------------------- Package

package com.tailor_pulp_website.back_end.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

// ----------------------------------------------- Java Spring Boot

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;




@Service
public class TokensService {

    
    private final JdbcTemplate jdbcTemplate;


    @Autowired
    public TokensService(JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }




    public int insertResetPasswordTokenInfo(int clientId, String resetPasswordToken) {

        try {

            return jdbcTemplate.update(
                """
                INSERT INTO reset_password_tokens (client_id, token, expires_at) 
                VALUES (?, ?, NOW() + INTERVAL '1 hour')
                ON CONFLICT (client_id) 
                DO
                UPDATE SET (token, expires_at)=(?, NOW() + INTERVAL '1 hour') WHERE reset_password_tokens.client_id=?
                """,
                clientId, resetPasswordToken, resetPasswordToken, clientId
            );
        }
        catch(DataAccessException e) {

            throw new RuntimeException("RuntimeException (insertResetPasswordTokenInfo) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    public Map<String, Object> getResetPasswordTokenInfo(String code) {

        try {

            List<Map<String, Object>> resetPasswordTokenRequest = jdbcTemplate.queryForList("SELECT * FROM reset_password_tokens WHERE token=? AND NOW() < expires_at", 
                code
            );

            if (resetPasswordTokenRequest.size() == 1 && resetPasswordTokenRequest.get(0) instanceof Map) {

                if (!(resetPasswordTokenRequest.get(0).isEmpty() && Arrays.stream(resetPasswordTokenRequest.get(0).values().toArray()).allMatch(el -> el != null))) {

                    return resetPasswordTokenRequest.get(0);
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

            throw new RuntimeException("RuntimeException (getResetPasswordTokenInfo) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }
}
