package com.pskehagias.soma.export;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

/**
 * Created by pkcyr on 6/13/2016.
 */
public class GmailSmtpSender {
    public static final String PROP_HOST = "mail.smtp.host";
    public static final String PROP_AUTH = "mail.smtps.auth";
    public static final String PROP_TLS = "mail.smtp.starttls.enable";

    public static final String HOST = "smtp.gmail.com";
    public static final String TRUE = "true";

    private Properties properties;

    public GmailSmtpSender(){
        properties = System.getProperties();
        properties.put(PROP_HOST, HOST);
        properties.put(PROP_AUTH, TRUE);
        properties.put(PROP_TLS, TRUE);
    }

    public void sendMail(String from, String passwd, String to, String filename) throws MessagingException {
        Session session = Session.getInstance(properties, null);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, to);
        message.setSubject("Daily Playlist Update");

        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText("Here's the file");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        messageBodyPart = new MimeBodyPart();

        DataSource source = new FileDataSource(filename);
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(filename);
        multipart.addBodyPart(messageBodyPart);
        message.setContent(multipart);

        try {
            Transport tr = session.getTransport("smtps");
            tr.connect(HOST, from, passwd);
            tr.sendMessage(message, message.getAllRecipients());
            System.out.println("Mail Sent Successfully");
            tr.close();
        } catch (SendFailedException sfe) {
            System.err.println(sfe.getMessage());
        }
    }
}
