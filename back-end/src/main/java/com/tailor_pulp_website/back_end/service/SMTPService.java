// ----------------------------------------------- Package

package com.tailor_pulp_website.back_end.service;

// ----------------------------------------------- Java Spring Boot

import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;




@Service
public class SMTPService {
    

    private final JavaMailSender javaMailSender;


    @Autowired
    public SMTPService(JavaMailSender javaMailSender) {

        this.javaMailSender = javaMailSender;
    }




    public void sendEmail(String from, String to, String subject, String message, boolean isHTML) {

        try {

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false);

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(message, isHTML);

            javaMailSender.send(mimeMessage);
        }
        catch(Exception e) {

            throw new RuntimeException("RuntimeException (sendEmail) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }
}
