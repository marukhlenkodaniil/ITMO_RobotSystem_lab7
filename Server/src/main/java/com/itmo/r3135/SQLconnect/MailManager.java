package com.itmo.r3135.SQLconnect;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class MailManager {
    static final Logger logger = LogManager.getLogger("MailManager");
    private Session session;
    private String username;
    private String password;
    private String host;
    private int port;
    private boolean auth;

    public MailManager(String username, String password, String host, int port, boolean auth) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
        this.auth = auth;
    }

    public boolean initMail() {
        logger.info("Mail Manager connect...");
        try {
            Properties prop = new Properties();
            prop.put("mail.smtp.host", host);
            prop.put("mail.smtp.port", port);
            prop.put("mail.smtp.auth", auth);
            prop.put("mail.smtp.socketFactory.port", port);
            prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            session = Session.getInstance(prop,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });
        } catch (Exception e) {
            logger.fatal("Mail Manager ERROR!");
            return false;
        }
        logger.info("Mail good connect!");
        return true;
    }

    public boolean sendMail(String eMail) {
        //session.setDebug(true);
        //пока тестовый текст
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(eMail)
            );
            message.setSubject("MESSAGE");
            message.setText("Message,"
                    + "\n\n Message!");
            Transport.send(message);

        } catch (MessagingException e) {
            logger.error("Send email message error!");
            return false;
        }
        return true;
    }
}