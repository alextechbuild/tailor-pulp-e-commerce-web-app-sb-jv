// ----------------------------------------------- Package

package com.tailor_pulp_website.back_end.components;

// ----------------------------------------------- Java Spring Boot

import org.springframework.stereotype.Component;

import com.tailor_pulp_website.back_end.service.ClientsService;
import com.tailor_pulp_website.back_end.service.SMTPService;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;




@Component
public class ScheduledTasks {
    

    private final AppConfig appConfig;
    private final ClientsService clientsService;
    private final SMTPService smtpService;


    @Autowired
    public ScheduledTasks(AppConfig appConfig, ClientsService clientsService, SMTPService smtpService) {

        this.appConfig = appConfig;
        this.clientsService = clientsService;
        this.smtpService = smtpService;
    }


    @Scheduled(cron = "0 30 11 * * FRI")
    public void sendNewsletter() {

        try {

            // Exécuté sur une seule instance (le leader spécifiquement)
            if (Boolean.parseBoolean(appConfig.getIsLeader())) {

                // A utiliser en mode production
                @SuppressWarnings("unused")
                List<Map<String, Object>> clientsList = clientsService.getClientIDsForNewsletter();

                PDDocument loadedPDFNewsletter = Loader.loadPDF(new File(appConfig.getNewsletterPath()));
                PDFTextStripper pdfTextStripper = new PDFTextStripper();

                String newsletterTextContent = pdfTextStripper.getText(loadedPDFNewsletter);

                // Remplacer HOST_EMAIL par ASSISTANCE_EMAIL en mode production
                smtpService.sendEmail(String.format("Tailor Pulp Company <" + appConfig.getHostEmail() + ">"), appConfig.getHostEmail(), "Your Newsletter", newsletterTextContent, true);
            
                System.out.println("Newsletter successfully sent");
            }
        }
        catch(Exception e) {

            throw new RuntimeException("RuntimeException (sendNewsletter) : " + e.getMessage() + "\n" + e.getStackTrace()[0]);
        }
    }
}
