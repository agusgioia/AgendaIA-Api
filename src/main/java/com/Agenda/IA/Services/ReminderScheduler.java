package com.Agenda.IA.Services;

import com.Agenda.IA.Models.Event;
import com.Agenda.IA.Repositories.EventRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReminderScheduler {

    private final EventRepository eventRepository;

    public ReminderScheduler(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Scheduled(fixedRate = 60000)
    public void checkReminders() {
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        List<Event> events = eventRepository.findAll();

        for (Event event : events) {
            if (event.getReminderMinutes() == null) continue;
            if (event.getUser().getFcmToken() == null) continue;

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
            Message message = Message.builder()
                    .setToken(event.getUser().getFcmToken())
                    .setNotification(Notification.builder()
                            .setTitle("Recordatorio — Agenda IA")
                            .setBody(event.getTitle())
                            .build())
                    .build();

            FirebaseMessaging.getInstance().send(message);
        } catch (Exception e) {
            System.err.println("Error enviando notificación: " + e.getMessage());
        }
    }
}