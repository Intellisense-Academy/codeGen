package com.mcode.llp.codegen.scheduler;

import com.mcode.llp.codegen.databases.OpenSearchClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationScheduler {
    private OpenSearchClient openSearchClient;
    @Autowired
    public NotificationScheduler(OpenSearchClient openSearchClient){
        this.openSearchClient=openSearchClient;
    }

    @Scheduled(cron = "0 0 11 * * *")
    public void sendDailyNotifications() {
        String endPoint = "/contributor/_search?filter_path=hits.hits._id";



    }
}
