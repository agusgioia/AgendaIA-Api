package com.Agenda.IA.Services;

import com.Agenda.IA.DTO.IntentResult;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PendingIntentService {
    private final Map<String, IntentResult> pendingIntents = new ConcurrentHashMap<>();

    public void save(String email, IntentResult intent) {
        pendingIntents.put(email, intent);
    }

    public IntentResult get(String email) {
        return pendingIntents.get(email);
    }

    public void clear(String email) {
        pendingIntents.remove(email);
    }
}