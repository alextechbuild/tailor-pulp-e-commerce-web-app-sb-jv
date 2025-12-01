// ----------------------------------------------- Package

package com.tailor_pulp_website.back_end.components;

// ----------------------------------------------- Java Spring Boot

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.springframework.stereotype.Component;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;




@Component
public class Cryptography {
    



    public String encryptPayload(String payload, String currentPublicKey) {

        try {

            JWEObject JWEObject = new JWEObject(

                new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM).build(),
                new Payload(payload)
            );


            byte[] decodedPublicKey = Base64.getDecoder().decode(currentPublicKey);
            X509EncodedKeySpec specPublicKey = new X509EncodedKeySpec(decodedPublicKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(specPublicKey);


            JWEObject.encrypt(new RSAEncrypter((RSAPublicKey) publicKey));
            String encryptedPayload = JWEObject.serialize();


            return encryptedPayload;
        }
        catch(Exception e) {

            throw new RuntimeException("RuntimeException (encryptPayload) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }




    public byte[] getRandomBytes() {

        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        return randomBytes;
    }




    public String decryptPayload(String payload, String currentPrivateKey, int t) {

        try {

            JWEObject jweObject = JWEObject.parse(payload);

            byte[] decodedPrivateKey = Base64.getDecoder().decode(currentPrivateKey);
            PKCS8EncodedKeySpec specPrivateKey = new PKCS8EncodedKeySpec(decodedPrivateKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(specPrivateKey);


            jweObject.decrypt(new RSADecrypter(privateKey));
            String decryptedPayload = jweObject.getPayload().toString();


            return decryptedPayload;
        }
        catch(Exception e) {

            throw new RuntimeException("RuntimeException (decryptPayload) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }
}
