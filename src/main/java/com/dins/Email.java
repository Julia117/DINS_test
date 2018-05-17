package com.dins;

import java.util.Date;
import java.util.Properties;
import javax.mail.*;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Email {

    public static void send(String text) {

        final String username = "example@yandex.ru";
        final String password = "password";
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.yandex.ru");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(username));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse("email_to@gmail.com",false));
            msg.setSubject("Metrics warning!");
            msg.setText(text);
            msg.setSentDate(new Date());
            Transport.send(msg);
        } catch (MessagingException e){
            System.out.println("Error: " + e);
        }
    }

}