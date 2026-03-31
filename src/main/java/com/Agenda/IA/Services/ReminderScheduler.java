package com.Agenda.IA.Services;

import com.Agenda.IA.Models.Event;
import com.Agenda.IA.Repositories.EventRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReminderScheduler {

    private final EventRepository eventRepository;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public ReminderScheduler(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Scheduled(fixedRate = 60000)
    public void checkReminders() {
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        List<Event> events = eventRepository.findAll();

        for (Event event : events) {
            if (event.getReminderMinutes() == null) continue;

            LocalDate date = event.getDate();
            LocalTime time = event.getTime() != null ? event.getTime() : LocalTime.of(8, 0);
            LocalDateTime eventDateTime = LocalDateTime.of(date, time);
            LocalDateTime triggerTime = eventDateTime.minusMinutes(event.getReminderMinutes());

            if (!triggerTime.equals(now)) continue;

            sendNotification(event);
        }
    }

    private void sendNotification(Event event) {
        try {
            String body = String.format(
                    "{\"app_id\":\"%s\"," +
                            "\"filters\":[{\"field\":\"tag\",\"key\":\"userId\",\"relation\":\"=\",\"value\":\"%d\"}]," +
                            "\"contents\":{\"en\":\"%s\"}," +
                            "\"headings\":{\"en\":\"Recordatorio — Agenda IA\"}}",
                    System.getenv("ONE_SIGNAL_ID"),
                    event.getUser().getId(),
                    event.getTitle()
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://onesignal.com/api/v1/notifications"))
                    .header("Authorization", "Basic " + System.getenv("ONE_SIGNAL_KEY"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            System.err.println("Error enviando notificación: " + e.getMessage());
        }
    }
}