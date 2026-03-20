package neevhrapi.co.uk.nit.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Properties;

@Service
public class EmailUtil {
    private static final Logger logger = LogManager.getLogger(EmailUtil.class);

    private static final String APPLICATION_NAME = "Gmail API JavaMail XOAUTH2";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = List.of("https://mail.google.com/");
    private static final String CREDENTIALS_FILE_PATH = "client_secret.json";

   /* private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        InputStream in = new FileInputStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }*/
   private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
       InputStream in = EmailUtil.class.getClassLoader().getResourceAsStream(CREDENTIALS_FILE_PATH);
       if (in == null) {
           throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
       }
       GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

       GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
               HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
               .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
               .setAccessType("offline")
               .build();

       return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
   }

    public void sendOutboundEmailAlerts(String subject, String msgbody, String toRecipients, String ccRecipients)
            throws MessagingException, GeneralSecurityException, IOException {

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = getCredentials(HTTP_TRANSPORT);

        if (credential.getExpiresInSeconds() <= System.currentTimeMillis()-2000) {
            // Access token has expired.  Refresh it.
            boolean refreshed = credential.refreshToken();  // Use the refreshToken() method
            if (!refreshed) {
                throw new MessagingException("Failed to refresh access token");
            }
            //  Persist the new access token.  The google client library usually handles this for you.
        }

        Properties props = new Properties();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth.mechanisms", "XOAUTH2");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        Session session = Session.getInstance(props);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress("admin@neevinfra.co.uk"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toRecipients));
        if (ccRecipients != null && !ccRecipients.trim().isEmpty()) { // Improved null/empty check
            message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ccRecipients));
        }
        message.setSubject(subject);
        message.setContent(msgbody, "text/html");
        Transport transport = session.getTransport("smtp");
        transport.connect("smtp.gmail.com", "admin@neevinfra.co.uk", credential.getAccessToken());
        transport.sendMessage(message, message.getAllRecipients());
        transport.close();
        logger.info("Email sent successfully using OAuth2!");
    }
}

