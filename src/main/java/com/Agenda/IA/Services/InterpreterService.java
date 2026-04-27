package com.Agenda.IA.Services;

import com.Agenda.IA.DTO.IntentResult;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
public class InterpreterService {

    private final AIService aiService;
    private final PendingIntentService pendingIntentService;

    public IntentResult interpret(String text, String email) {
        text = text.toLowerCase();

        IntentResult r = new IntentResult();

        // 1. INTENT (Lógica manual)
        if (text.contains("agendar") || text.contains("agenda")) {
            r.setIntent("create_event");
        } else if (text.contains("hoy")) {
            r.setIntent("read_today");
        } else if (text.contains("semana")) {
            r.setIntent("read_week");
        } else {
            // Si no detectamos palabra clave, vamos directo a la IA con el email
            return aiService.interpret(text, email, pendingIntentService);
        }

        if (text.contains("mañana")) {
            r.setDate(LocalDate.now().plusDays(1));
        } else if (text.contains("hoy")) {
            r.setDate(LocalDate.now());
        }

        Pattern p = Pattern.compile("(\\d{1,2})(:\\d{2})?");
        Matcher m = p.matcher(text);

        if (m.find()) {
            int hour = Integer.parseInt(m.group(1));
            int min = m.group(2) != null ? Integer.parseInt(m.group(2).substring(1)) : 0;
            r.setTime(LocalTime.of(hour, min));
        }

        // 3. VALIDACIÓN CLAVE
        if ("create_event".equals(r.getIntent())) {
            if (r.getDate() == null) {
                return aiService.interpret(text, email, pendingIntentService);
            }

            String clean = text
                    .replaceAll("(?i)\\b(agendar|agenda|mañana|hoy)\\b", "")
                    .replaceAll("a las \\d{1,2}(?::\\d{2})?", "")
                    .replaceAll("\\s+", " ")
                    .trim();

            if (clean.isEmpty()) {
                return aiService.interpret(text, email, pendingIntentService);
            }
            r.setTitle(clean);
        }

        return r;
    }
}