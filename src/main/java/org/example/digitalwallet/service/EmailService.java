package org.example.digitalwallet.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender emailSender;

    public EmailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void sendWalletCreationEmail(String to , String username, String currency , String balance) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Digital Wallet Created");
        message.setText("Hello " + username + ",\n\n" +
                "Your new " + currency + " wallet has been successfully created.\n" +
                "Initial Balance: " + balance);

        emailSender.send(message);
    }

    public void sendEmailOnDeposit(String to , String username, String currency , String deposit , String balance) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject("Successful deposit");
        message.setText("Hello " + username + ",\n\n" +
                "Thanks for trusting us and depositing " + deposit + " of " + currency + "\n" +
                "New balance is " + balance);

        emailSender.send(message);
    }
}
