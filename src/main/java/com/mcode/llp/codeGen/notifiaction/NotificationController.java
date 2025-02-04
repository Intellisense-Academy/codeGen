package com.mcode.llp.codeGen.notifiaction;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {


    @Autowired
    private SmsService smsService;

    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber;

    @PostMapping("/sendSms")
    public String sendSms(@RequestBody SmsRequest smsRequest) {
        try {
            Message message = Message.creator(
                    new PhoneNumber(smsRequest.getTo()), // recipient phone number
                    new PhoneNumber(twilioPhoneNumber), // your Twilio phone number
                    smsRequest.getBody() // message body
            ).create();

            return "Message sent successfully! SID: " + message.getSid();
        } catch (Exception e) {
            return "Error sending message: " + e.getMessage();
        }
    }
}
