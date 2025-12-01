// ----------------------------------------------- Package

package com.tailor_pulp_website.back_end.interceptor;

// ----------------------------------------------- Java Spring Boot

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;




@Configuration
public class WebConfig implements WebMvcConfigurer {
    

    private final CleanInterceptor cleanInterceptor;
    private final AuthenticationInterceptor authenticationInterceptor;


    @Autowired
    public WebConfig(AuthenticationInterceptor authenticationInterceptor, CleanInterceptor cleanInterceptor) {

        this.authenticationInterceptor = authenticationInterceptor;
        this.cleanInterceptor = cleanInterceptor;
    }


    @Override
    public void addInterceptors(InterceptorRegistry interceptorRegistry) {

        
        interceptorRegistry.addInterceptor(cleanInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns("/loadImgs");


        interceptorRegistry.addInterceptor(authenticationInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns("/login")
        .excludePathPatterns("/GoogleLogin")
        .excludePathPatterns("/logout")
        .excludePathPatterns("/sendVerificationCode")
        .excludePathPatterns("/modifyClientPassword")
        .excludePathPatterns("/confirm-2fa")
        .excludePathPatterns("/sendFormMessage")
        .excludePathPatterns("/loadImgs")
        .excludePathPatterns("/getSizesAndQuantities")
        .excludePathPatterns("/getProductQuantity")
        .excludePathPatterns("/signup");
    }


    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {

        corsRegistry.addMapping("/**")
        .allowedOrigins("https://localhost:3000")
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
        .allowedHeaders("*")
        .allowCredentials(true);
    }
}
