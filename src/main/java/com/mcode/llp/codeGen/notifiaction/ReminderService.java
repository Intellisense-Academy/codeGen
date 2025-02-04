package com.mcode.llp.codeGen.notifiaction;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class ReminderService {

    @Autowired
    private NotificationRepository repository;

    @Autowired
    private SmsService smsService ;

    private static final String THANK_YOU_MESSAGE = "Thank you %s for your payment of INR %.2f. We appreciate your promptness!";
    private static final String REMAINDER_MESSAGE = "Hello %s, your payment of INR %.2f is due. Kindly pay before %s to avoid penalties.";

    @Scheduled(cron = "0 0 10 5 2 ? 2025")
    public void sendReminders() {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);

        LocalDate startDate = currentMonth.atDay(1); // First day of the month
        LocalDate endDate = currentMonth.atDay(5);   // Fifth day of the month


        List<ClientDetails> paidClients = repository.findByIsPaidTrueAndPaymentDateBetween(startDate, endDate);
        for (ClientDetails client : paidClients) {
            String message = String.format(THANK_YOU_MESSAGE, client.getClientName(), client.getRemainderAmount());
            smsService.sendSms(client.getContactNumber(), message);
        }


        List<ClientDetails> unpaidClients = repository.findByIsPaidFalse();
        for (ClientDetails client : unpaidClients) {
            String message = String.format(REMAINDER_MESSAGE, client.getClientName(), client.getRemainderAmount(), today.plusDays(7));
            smsService.sendSms(client.getContactNumber(), message);
        }
    }

}
