package com.mcode.llp.codeGen.notifiaction;
import com.twilio.Twilio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TwilioConfig {

    private final String accountSid;
    private final String authToken;
    private final String phoneNumber;

    public TwilioConfig(
            @Value("${twilio.account.sid}") String accountSid,
            @Value("${twilio.auth.token}") String authToken,
            @Value("${twilio.phone.number}") String phoneNumber) {

        if (accountSid == null || authToken == null) {
            throw new IllegalArgumentException("Twilio credentials are not set");
        }

        this.accountSid = accountSid;
        this.authToken = authToken;
        this.phoneNumber = phoneNumber;

        Twilio.init(accountSid, authToken);
        System.out.println("Twilio initialized successfully.");
    }

    public String getAccountSid() {
        return accountSid;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
