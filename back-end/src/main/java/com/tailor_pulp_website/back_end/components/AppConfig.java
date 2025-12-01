// ----------------------------------------------- Package

package com.tailor_pulp_website.back_end.components;

// ----------------------------------------------- Java Spring Boot

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;




@Component
public class AppConfig {


    @Value("${jwt.secret}")
    private String JWT_SECRET;


    public String getJwtSecret() {
        return this.JWT_SECRET;
    }


    @Value("${cookie.secret}")
    private String COOKIE_SECRET;

    public String getCookieSecret() {

        return this.COOKIE_SECRET;
    }


    @Value("${google.oauth.client.id}")
    private String GOOGLE_OAUTH_CLIENT_ID;

    public String getGoogleOAuthClientID() {

        return this.GOOGLE_OAUTH_CLIENT_ID;
    }

    
    @Value("${public.key}")
    private String PUBLIC_KEY;

    public String getPublicKey() {

        return this.PUBLIC_KEY;
    }


    @Value("${private.key}")
    private String PRIVATE_KEY;

    public String getPrivateKey() {

        return this.PRIVATE_KEY;
    }


    @Value("${host.email}")
    private String HOST_EMAIL;

    public String getHostEmail() {

        return this.HOST_EMAIL;
    }


    @Value("${assistance.email}")
    private String ASSISTANCE_EMAIL;

    public String getAssistanceEmail() {

        return this.ASSISTANCE_EMAIL;
    }


    @Value("${recaptcha.secret.key}")
    private String RECAPTCHA_SECRET_KEY;

    public String getRecaptchaSecretKey() {

        return this.RECAPTCHA_SECRET_KEY;
    }


    @Value("${front.end.url}")
    private String FRONT_END_URL;

    public String getFrontEndURL() {

        return this.FRONT_END_URL;
    }


    @Value("${stripe.secret.key}")
    private String STRIPE_SECRET_KEY;

    public String getStripeSecretKey() {

        return this.STRIPE_SECRET_KEY;
    }


    @Value("${is.leader}")
    private String IS_LEADER;

    public String getIsLeader() {

        return this.IS_LEADER;
    }


    @Value("${newsletter.path}")
    private String newsletterPath;

    public String getNewsletterPath() {

        return this.newsletterPath;
    }
}
