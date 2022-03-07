package com.example.demo.services;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import com.example.demo.constant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class serverService {
    /*@Autowired
    private JavaMailSender mailSender;

     public void sendEmailOut() throws UnsupportedEncodingException, MessagingException{
        String subject = "Please verify your registration.";
        String senderName="Starlet App Team";
        String mailContent="<p>Dear!<p>";
        mailContent +="<p>Thanks! <br> The Starlet App Team<p>";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper =  new MimeMessageHelper(message);
        helper.setFrom(constant.APP_DEFAULT_EMAIL,senderName);
        helper.setTo("joechinjiahao@gmail.com");
        helper.setSubject(subject);
        helper.setText(mailContent, true);
        mailSender.send(message);
    } */
    

    @Bean
    public JavaMailSender getJavaMailSender(){
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(constant.EMAIL_HOST);
        mailSender.setPort(constant.EMAIL_PORT);
        mailSender.setUsername(constant.EMAIL_USERNAME);
        mailSender.setPassword(constant.EMAIL_PASSWORD);
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol","smtp");
        props.put("mail.smtp.auth","true");
        props.put("mail.smtp.starttls.enable","true");
        props.put("mail.debug",true);
        return mailSender;
    }
}
